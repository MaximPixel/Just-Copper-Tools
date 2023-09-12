package net.maximpixel.jct.block;

import net.maximpixel.jct.JustCopperTools;
import net.maximpixel.jct.block.entity.CopperHopperBlockEntity;
import net.maximpixel.jct.block.entity.CopperItemStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class CopperItemStandBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    protected static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 11, 15);

    public CopperItemStandBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return JustCopperTools.COPPER_ITEM_STAND_BLOCK_ENTITY_TYPE.get().create(blockPos, blockState);
    }

    @NotNull
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof CopperItemStandBlockEntity) {
                ItemStack storedItem = ((CopperItemStandBlockEntity) blockEntity).getItem(0);

                if (storedItem.isEmpty()) {
                    ItemStack stack = player.getItemInHand(interactionHand);
                    if (!stack.isEmpty()) {
                        ((CopperItemStandBlockEntity) blockEntity).setItem(0, stack.split(1));

                        if (player.isCreative()) {
                            stack.grow(1);
                        }
                    }
                } else {
                    ItemStack stack = ((CopperItemStandBlockEntity) blockEntity).removeItem(0, 1);
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, JustCopperTools.COPPER_ITEM_STAND_BLOCK_ENTITY_TYPE.get(), CopperItemStandBlockEntity::tickAge);
    }

    @Override
    public boolean isPathfindable(BlockState arg, BlockGetter arg2, BlockPos arg3, PathComputationType arg4) {
        return false;
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);

        if (!level.isClientSide && level.hasNeighborSignal(blockPos)) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof CopperItemStandBlockEntity) {
                ItemStack storedItem = ((CopperItemStandBlockEntity) blockEntity).getItem(0);

                if (storedItem.isEmpty()) {
                    List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AABB(blockPos).move(0D, 0D, 0.5D));

                    for (ItemEntity itemEntity : itemEntities) {
                        ItemStack stack = itemEntity.getItem().split(1);
                        ((CopperItemStandBlockEntity) blockEntity).setItem(0, stack);

                        if (stack.isEmpty()) {
                            itemEntity.discard();
                        }
                    }
                } else {
                    ItemStack stack = ((CopperItemStandBlockEntity) blockEntity).removeItem(0, 1);
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + 0.5D, blockPos.getY() + 1D, blockPos.getZ() + 0.5D, stack);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState.is(blockState2.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof CopperItemStandBlockEntity) {
                Containers.dropContents(level, blockPos, (Container) blockEntity);
                level.updateNeighbourForOutputSignal(blockPos, this);
            }

            super.onRemove(blockState, level, blockPos, blockState2, bl);
        }
    }
}
