/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.block;

import fr.dev1lroot.mcmods.littlecraft.content.ChangingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChangingTableBlockEntity extends BlockEntity
{
    private final DyeColor color;

    public ChangingTableBlockEntity(BlockPos pos, BlockState state)
    {
        super(ChangingTable.CHANGING_TABLE_BLOCK_ENTITY.get(), pos, state);
        this.color = ((BedBlock) state.getBlock()).getColor();
    }

    public DyeColor getColor() { return color; }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
