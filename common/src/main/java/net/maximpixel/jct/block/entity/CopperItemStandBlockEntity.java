package net.maximpixel.jct.block.entity;

import net.maximpixel.jct.JustCopperTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CopperItemStandBlockEntity extends BlockEntity implements Container {

    public static void tickAge(Level level, BlockPos blockPos, BlockState blockState, CopperItemStandBlockEntity blockEntity) {
        blockEntity.age++;
    }

    private ItemStack stack = ItemStack.EMPTY;
    private int age = 0;

    public CopperItemStandBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(JustCopperTools.COPPER_ITEM_STAND_BLOCK_ENTITY_TYPE.get(), blockPos, blockState);
    }

    public CopperItemStandBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public int getAge() {
        return age;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @NotNull
    @Override
    public ItemStack getItem(int i) {
        return i == 0 ? stack : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack removeItem(int i, int j) {
        if (i == 0) {
            onInventoryChanged();
            return stack.split(j);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int i) {
        if (i == 0) {
            ItemStack temp = stack;
            stack = ItemStack.EMPTY;
            return temp;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (i == 0) {
            stack = itemStack;
        }
        onInventoryChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        stack = ItemStack.EMPTY;
        onInventoryChanged();
    }

    protected void onInventoryChanged() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        stack = ItemStack.of(compoundTag.getCompound("Item"));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        CompoundTag tag = new CompoundTag();
        stack.save(tag);
        compoundTag.put("Item", tag);
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
}
