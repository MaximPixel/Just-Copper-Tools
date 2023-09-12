package net.maximpixel.jct.block.entity;

import net.maximpixel.jct.JustCopperTools;
import net.maximpixel.jct.inventory.CopperHopperMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CopperHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper, FilterContainer {

    public static final int MOVE_ITEM_SPEED = 8;
    public static final int HOPPER_CONTAINER_SIZE = 5;

    public static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, CopperHopperBlockEntity copperHopperBlockEntity) {
        --copperHopperBlockEntity.cooldownTime;
        copperHopperBlockEntity.tickedGameTime = level.getGameTime();
        if (!copperHopperBlockEntity.isOnCooldown()) {
            copperHopperBlockEntity.setCooldown(0);

            if (!level.isClientSide) {
                tryMoveItems(level, blockPos, blockState, copperHopperBlockEntity, () -> suckInItems(level, copperHopperBlockEntity));
            }
        }
    }

    public static void entityInside(Level level, BlockPos blockPos, BlockState blockState, Entity entity, CopperHopperBlockEntity copperHopperBlockEntity) {
        if (level.isClientSide || copperHopperBlockEntity.isOnCooldown()) {
            return;
        }

        if (entity instanceof ItemEntity && Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ())), copperHopperBlockEntity.getSuckShape(), BooleanOp.AND)) {
            tryMoveItems(level, blockPos, blockState, copperHopperBlockEntity, () -> addItem(copperHopperBlockEntity, (ItemEntity)entity));
        }
    }

    private static void tryMoveItems(Level level, BlockPos blockPos, BlockState blockState, CopperHopperBlockEntity copperHopperBlockEntity, BooleanSupplier booleanSupplier) {
        if (blockState.getValue(HopperBlock.ENABLED)) {
            boolean bl = false;

            if (!copperHopperBlockEntity.isEmpty()) {
                bl = ejectItems(level, blockPos, blockState, copperHopperBlockEntity, true) || ejectItems(level, blockPos, blockState, copperHopperBlockEntity, false);
            }

            if (!copperHopperBlockEntity.inventoryFull()) {
                bl |= booleanSupplier.getAsBoolean();
            }

            if (bl) {
                copperHopperBlockEntity.setCooldown(MOVE_ITEM_SPEED);
                setChanged(level, blockPos, blockState);
            }
        }
    }

    private static boolean ejectItems(Level level, BlockPos blockPos, BlockState blockState, CopperHopperBlockEntity copperHopperBlockEntity, boolean filter) {
        ItemStack filterItem = copperHopperBlockEntity.getFilterItem();

        if (filter && filterItem.isEmpty()) {
            return false;
        }

        for (int i = 0; i < copperHopperBlockEntity.getContainerSize(); i++) {
            ItemStack stack = copperHopperBlockEntity.getItem(i);

            if (filter == copperHopperBlockEntity.getFilterType().filter(filterItem, stack)) {
                Direction direction = filter ? Direction.DOWN : blockState.getValue(HopperBlock.FACING);
                Container containerTo =  getContainerAt(level, blockPos.relative(direction));

                if (containerTo != null) {
                    stack = stack.copy();
                    ItemStack a = addItem(copperHopperBlockEntity, containerTo, copperHopperBlockEntity.removeItem(i, 1), direction.getOpposite());

                    if (a.isEmpty()) {
                        containerTo.setChanged();
                        return true;
                    }

                    copperHopperBlockEntity.setItem(i, stack);
                }
            }
        }

        return false;
    }

    private static IntStream getSlots(Container container, Direction direction) {
        return container instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)container).getSlotsForFace(direction)) : IntStream.range(0, container.getContainerSize());
    }

    private static boolean isEmptyContainer(Container container, Direction direction) {
        return getSlots(container, direction).allMatch((i) -> container.getItem(i).isEmpty());
    }

    public static boolean suckInItems(Level level, Hopper hopper) {
        Container container = getSourceContainer(level, hopper);
        if (container != null) {
            Direction direction = Direction.DOWN;
            return !isEmptyContainer(container, direction) && getSlots(container, direction).anyMatch((i) -> tryTakeInItemFromSlot(hopper, container, i, direction));
        } else {
            Iterator<ItemEntity> var3 = getItemsAtAndAbove(level, hopper).iterator();

            ItemEntity itemEntity;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                itemEntity = var3.next();
            } while(!addItem(hopper, itemEntity));

            return true;
        }
    }

    private static boolean tryTakeInItemFromSlot(Hopper hopper, Container container, int i, Direction direction) {
        ItemStack itemStack = container.getItem(i);
        if (!itemStack.isEmpty() && canTakeItemFromContainer(hopper, container, itemStack, i, direction)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = addItem(container, hopper, container.removeItem(i, 1), null);
            if (itemStack3.isEmpty()) {
                container.setChanged();
                return true;
            }

            container.setItem(i, itemStack2);
        }

        return false;
    }

    public static boolean addItem(Container container, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getItem().copy();
        ItemStack itemStack2 = addItem(null, container, itemStack, null);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack2);
        }

        return bl;
    }

    public static ItemStack addItem(@Nullable Container container, Container container2, ItemStack itemStack, @Nullable Direction direction) {
        int i;
        if (container2 instanceof WorldlyContainer worldlyContainer) {
            if (direction != null) {
                int[] is = worldlyContainer.getSlotsForFace(direction);

                for (i = 0; i < is.length && !itemStack.isEmpty(); ++i) {
                    itemStack = tryMoveInItem(container, container2, itemStack, is[i], direction);
                }

                return itemStack;
            }
        }

        int j = container2.getContainerSize();

        for (i = 0; i < j && !itemStack.isEmpty(); ++i) {
            itemStack = tryMoveInItem(container, container2, itemStack, i, direction);
        }

        return itemStack;
    }

    private static boolean canPlaceItemInContainer(Container container, ItemStack itemStack, int i, @Nullable Direction direction) {
        if (!container.canPlaceItem(i, itemStack)) {
            return false;
        } else {
            boolean var10000;
            if (container instanceof WorldlyContainer) {
                WorldlyContainer worldlyContainer = (WorldlyContainer)container;
                if (!worldlyContainer.canPlaceItemThroughFace(i, itemStack, direction)) {
                    var10000 = false;
                    return var10000;
                }
            }

            var10000 = true;
            return var10000;
        }
    }

    private static boolean canTakeItemFromContainer(Container container, Container container2, ItemStack itemStack, int i, Direction direction) {
        if (!container2.canTakeItem(container, i, itemStack)) {
            return false;
        } else {
            boolean var10000;
            if (container2 instanceof WorldlyContainer) {
                WorldlyContainer worldlyContainer = (WorldlyContainer)container2;
                if (!worldlyContainer.canTakeItemThroughFace(i, itemStack, direction)) {
                    var10000 = false;
                    return var10000;
                }
            }

            var10000 = true;
            return var10000;
        }
    }

    private static ItemStack tryMoveInItem(@Nullable Container container, Container container2, ItemStack itemStack, int i, @Nullable Direction direction) {
        ItemStack itemStack2 = container2.getItem(i);
        if (canPlaceItemInContainer(container2, itemStack, i, direction)) {
            boolean bl = false;
            boolean bl2 = container2.isEmpty();
            if (itemStack2.isEmpty()) {
                container2.setItem(i, itemStack);
                itemStack = ItemStack.EMPTY;
                bl = true;
            } else if (canMergeItems(itemStack2, itemStack)) {
                int j = itemStack.getMaxStackSize() - itemStack2.getCount();
                int k = Math.min(itemStack.getCount(), j);
                itemStack.shrink(k);
                itemStack2.grow(k);
                bl = k > 0;
            }

            if (bl) {
                if (bl2 && container2 instanceof CopperHopperBlockEntity) {
                    CopperHopperBlockEntity copperHopperBlockEntity = (CopperHopperBlockEntity)container2;
                    if (!copperHopperBlockEntity.isOnCustomCooldown()) {
                        int k = 0;
                        if (container instanceof CopperHopperBlockEntity) {
                            CopperHopperBlockEntity hopperBlockEntity2 = (CopperHopperBlockEntity)container;
                            if (copperHopperBlockEntity.tickedGameTime >= hopperBlockEntity2.tickedGameTime) {
                                k = 1;
                            }
                        }

                        copperHopperBlockEntity.setCooldown(MOVE_ITEM_SPEED - k);
                    }
                }

                container2.setChanged();
            }
        }

        return itemStack;
    }

    @Nullable
    private static Container getSourceContainer(Level level, Hopper hopper) {
        return getContainerAt(level, hopper.getLevelX(), hopper.getLevelY() + 1D, hopper.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level level, Hopper hopper) {
        return hopper.getSuckShape().toAabbs().stream().flatMap((aABB) -> level.getEntitiesOfClass(ItemEntity.class, aABB.move(hopper.getLevelX() - 0.5, hopper.getLevelY() - 0.5, hopper.getLevelZ() - 0.5), EntitySelector.ENTITY_STILL_ALIVE).stream()).collect(Collectors.toList());
    }

    @Nullable
    public static Container getContainerAt(Level level, BlockPos blockPos) {
        return getContainerAt(level, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D);
    }

    @Nullable
    private static Container getContainerAt(Level level, double d, double e, double f) {
        Container container = null;
        BlockPos blockPos = BlockPos.containing(d, e, f);
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            container = ((WorldlyContainerHolder)block).getContainer(blockState, level, blockPos);
        } else if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof Container) {
                container = (Container)blockEntity;
                if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    container = ChestBlock.getContainer((ChestBlock)block, blockState, level, blockPos, true);
                }
            }
        }

        if (container == null) {
            List<Entity> list = level.getEntities((Entity)null, new AABB(d - 0.5D, e - 0.5D, f - 0.5D, d + 0.5D, e + 0.5D, f + 0.5D), EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!list.isEmpty()) {
                container = (Container)list.get(level.random.nextInt(list.size()));
            }
        }

        return container;
    }

    private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getCount() <= itemStack.getMaxStackSize() && ItemStack.isSameItemSameTags(itemStack, itemStack2);
    }

    protected NonNullList<ItemStack> items;
    protected int cooldownTime;
    protected long tickedGameTime;
    protected ItemStack filterItem = ItemStack.EMPTY;
    protected FilterType filterType = FilterTypes.NONE;

    public CopperHopperBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(JustCopperTools.COPPER_HOPPER_BLOCK_ENTITY_TYPE.get(), blockPos, blockState);
    }

    public CopperHopperBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        items = NonNullList.withSize(HOPPER_CONTAINER_SIZE, ItemStack.EMPTY);
        cooldownTime = -1;
    }

    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        items = NonNullList.withSize(HOPPER_CONTAINER_SIZE, ItemStack.EMPTY);
        if (!tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, items);
        }

        cooldownTime = compoundTag.getInt("TransferCooldown");

        filterItem = ItemStack.of(compoundTag.getCompound("Filter"));

        String filterTypeStr = compoundTag.getString("FilterType");
        filterType = Arrays.stream(FilterTypes.values())
                .filter(f -> f.toString().equals(filterTypeStr))
                .findAny()
                .orElse(FilterTypes.NONE);

        if (level != null && level.isClientSide) {
            var mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof CopperHopperMenu) {
                ((CopperHopperMenu) mc.player.containerMenu).getFilterContainer().setFilterItem(filterItem);
                ((CopperHopperMenu) mc.player.containerMenu).getFilterContainer().setFilterType(filterType);
            }
        }
    }

    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (!trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, items);
        }

        compoundTag.putInt("TransferCooldown", cooldownTime);

        compoundTag.put("Filter", filterItem.save(new CompoundTag()));
        compoundTag.putString("FilterType", filterType.toString());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public int getContainerSize() {
        return items.size();
    }

    public ItemStack removeItem(int i, int j) {
        unpackLootTable(null);
        return ContainerHelper.removeItem(getItems(), i, j);
    }

    public void setItem(int i, ItemStack itemStack) {
        unpackLootTable(null);
        getItems().set(i, itemStack);
        if (itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }

    }

    protected Component getDefaultName() {
        return Component.translatable("container.jct.copper_hopper");
    }

    private boolean inventoryFull() {
        Iterator<ItemStack> var1 = items.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = var1.next();
        } while(!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxStackSize());

        return false;
    }

    public double getLevelX() {
        return (double) worldPosition.getX() + 0.5D;
    }

    public double getLevelY() {
        return (double) worldPosition.getY() + 0.5D;
    }

    public double getLevelZ() {
        return (double) worldPosition.getZ() + 0.5D;
    }

    private void setCooldown(int i) {
        cooldownTime = i;
    }

    private boolean isOnCooldown() {
        return cooldownTime > 0;
    }

    private boolean isOnCustomCooldown() {
        return cooldownTime > MOVE_ITEM_SPEED;
    }

    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    protected void setItems(NonNullList<ItemStack> nonNullList) {
        items = nonNullList;
    }

    @NotNull
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new CopperHopperMenu(i, inventory, this, this);
    }

    @Override
    public ItemStack getFilterItem() {
        return filterItem;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public void setFilterItem(ItemStack filterItem) {
        this.filterItem = filterItem;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
}
