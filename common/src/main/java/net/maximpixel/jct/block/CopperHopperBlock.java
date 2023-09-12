package net.maximpixel.jct.block;

import net.maximpixel.jct.JustCopperTools;
import net.maximpixel.jct.block.entity.CopperHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CopperHopperBlock extends HopperBlock {

    protected static final VoxelShape EAST_SHAPE;
    protected static final VoxelShape NORTH_SHAPE;
    protected static final VoxelShape SOUTH_SHAPE;
    protected static final VoxelShape WEST_SHAPE;
    protected static final VoxelShape EAST_INTERACTION_SHAPE;
    protected static final VoxelShape NORTH_INTERACTION_SHAPE;
    protected static final VoxelShape SOUTH_INTERACTION_SHAPE;
    protected static final VoxelShape WEST_INTERACTION_SHAPE;

    static {
        VoxelShape top = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
        VoxelShape funnel = Block.box(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
        VoxelShape convexBase = Shapes.or(funnel, top);
        VoxelShape base = Shapes.join(convexBase, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
        base = Shapes.or(base, Block.box(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
        EAST_SHAPE = Shapes.or(base, Block.box(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
        NORTH_SHAPE = Shapes.or(base, Block.box(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
        SOUTH_SHAPE = Shapes.or(base, Block.box(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
        WEST_SHAPE = Shapes.or(base, Block.box(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
        EAST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(12.0, 8.0, 6.0, 16.0, 10.0, 10.0));
        NORTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0, 8.0, 0.0, 10.0, 10.0, 4.0));
        SOUTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0, 8.0, 12.0, 10.0, 10.0, 16.0));
        WEST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(0.0, 8.0, 6.0, 4.0, 10.0, 10.0));
    }

    public CopperHopperBlock(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return switch (blockState.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> EAST_SHAPE;
        };
    }

    @NotNull
    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return switch (blockState.getValue(FACING)) {
            case NORTH -> NORTH_INTERACTION_SHAPE;
            case SOUTH -> SOUTH_INTERACTION_SHAPE;
            case WEST -> WEST_INTERACTION_SHAPE;
            default -> EAST_INTERACTION_SHAPE;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return JustCopperTools.COPPER_HOPPER_BLOCK_ENTITY_TYPE.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide) {
            return createTickerHelper(blockEntityType, JustCopperTools.COPPER_HOPPER_BLOCK_ENTITY_TYPE.get(), CopperHopperBlockEntity::pushItemsTick);
        }
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return defaultBlockState()
                .setValue(FACING, blockPlaceContext.getHorizontalDirection())
                .setValue(ENABLED, true);
    }

    @NotNull
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            var blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof CopperHopperBlockEntity && player instanceof ServerPlayer) {
                player.openMenu((CopperHopperBlockEntity) blockEntity);
                ((ServerPlayer) player).connection.send(blockEntity.getUpdatePacket());
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void entityInside(BlockState arg, Level arg2, BlockPos arg3, Entity arg4) {
        BlockEntity blockEntity = arg2.getBlockEntity(arg3);
        if (blockEntity instanceof CopperHopperBlockEntity) {
            CopperHopperBlockEntity.entityInside(arg2, arg3, arg, arg4, (CopperHopperBlockEntity) blockEntity);
        }
    }
}
