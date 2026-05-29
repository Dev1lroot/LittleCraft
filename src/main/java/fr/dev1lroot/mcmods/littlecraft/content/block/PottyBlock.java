/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.block;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.Potty;
import fr.dev1lroot.mcmods.littlecraft.content.entity.PottySeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.BiConsumer;

public class PottyBlock extends BaseEntityBlock
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public static final MapCodec<PottyBlock> CODEC = simpleCodec(PottyBlock::new);

    public PottyBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<PottyBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new PottyBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        List<PottySeatEntity> existing = level.getEntities(
            EntityTypeTest.forClass(PottySeatEntity.class),
            new AABB(pos).inflate(0.5),
            e -> pos.equals(e.getPottyPos())
        );
        if (!existing.isEmpty()) return InteractionResult.PASS;

        PottySeatEntity seat = new PottySeatEntity(level, pos);
        level.addFreshEntity(seat);
        player.startRiding(seat, true, false);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston)
    {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        level.getEntities(
            EntityTypeTest.forClass(PottySeatEntity.class),
            new AABB(pos).inflate(0.5),
            e -> pos.equals(e.getPottyPos())
        ).forEach(e -> { e.ejectPassengers(); e.discard(); });
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                               BlockEntity blockEntity, ItemStack tool)
    {
        if (!level.isClientSide() && blockEntity instanceof PottyBlockEntity entity)
            Block.popResource(level, pos, createDrop(entity));
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
                                   BiConsumer<ItemStack, BlockPos> dropConsumer)
    {
        if (level.getBlockEntity(pos) instanceof PottyBlockEntity entity)
            dropConsumer.accept(createDrop(entity), pos);
        else
            super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    private ItemStack createDrop(PottyBlockEntity entity)
    {
        ItemStack stack = new ItemStack(Potty.POTTY_ITEM.get());
        if (entity.getPiss() > 0 || entity.getPoop() > 0)
        {
            TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            output.putInt(PottyBlockEntity.TAG_PISS, entity.getPiss());
            output.putInt(PottyBlockEntity.TAG_POOP, entity.getPoop());
            BlockItem.setBlockEntityData(stack, (BlockEntityType<?>) Potty.POTTY_BLOCK_ENTITY.get(), output);
        }
        return stack;
    }
}
