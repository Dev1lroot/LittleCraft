/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CribBlock extends BedBlock
{
    @SuppressWarnings("unchecked")
    private static final MapCodec<BedBlock> CODEC =
        (MapCodec<BedBlock>) (MapCodec<?>) simpleCodec(CribBlock::new);

    public CribBlock(BlockBehaviour.Properties properties)
    {
        super(DyeColor.WHITE, properties);
    }

    @Override
    public MapCodec<BedBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CribBlockEntity(pos, state);
    }
}
