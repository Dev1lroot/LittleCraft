/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.block;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class AbstractChangingTableBlock extends BedBlock
{
    @SuppressWarnings("unchecked")
    private static final MapCodec<BedBlock> CODEC =
        (MapCodec<BedBlock>) (MapCodec<?>) simpleCodec(
            props -> new AbstractChangingTableBlock(DyeColor.WHITE, props));

    public AbstractChangingTableBlock(DyeColor color, BlockBehaviour.Properties properties)
    {
        super(color, properties);
    }

    @Override
    public MapCodec<BedBlock> codec() { return CODEC; }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ChangingTableBlockEntity(pos, state);
    }

    /**
     * Completely replaces the vanilla bed sleep logic with a riding-entity approach.
     * The player mounts a {@link ChangingTableSeatEntity} that forces Pose.SLEEPING
     * each tick, giving the lying-down visual with zero interaction with the sleep system.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit)
    {
        if (level.isClientSide()) return InteractionResult.SUCCESS_SERVER;

        // Always work from the HEAD half position (that's where the seat entity lives).
        Direction facing = state.getValue(FACING);
        BlockPos headPos = (state.getValue(PART) == BedPart.FOOT)
            ? pos.relative(facing)
            : pos;

        // Verify the head block is still present.
        if (!level.getBlockState(headPos).is(this)) return InteractionResult.CONSUME;

        // One rider at a time.
        List<ChangingTableSeatEntity> existing = level.getEntities(
            EntityTypeTest.forClass(ChangingTableSeatEntity.class),
            new AABB(headPos).inflate(1.0),
            e -> headPos.equals(e.getTablePos())
        );
        if (!existing.isEmpty()) return InteractionResult.PASS;

        ChangingTableSeatEntity seat = new ChangingTableSeatEntity(level, headPos, facing);
        level.addFreshEntity(seat);
        player.startRiding(seat, true, false);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level,
                                               BlockPos pos, boolean movedByPiston)
    {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);

        // Resolve HEAD position regardless of which half was removed.
        Direction facing = state.getValue(FACING);
        BlockPos headPos = (state.getValue(PART) == BedPart.HEAD)
            ? pos
            : pos.relative(facing);

        level.getEntities(
            EntityTypeTest.forClass(ChangingTableSeatEntity.class),
            new AABB(headPos).inflate(0.5),
            e -> headPos.equals(e.getTablePos())
        ).forEach(e -> { e.ejectPassengers(); e.discard(); });
    }
}
