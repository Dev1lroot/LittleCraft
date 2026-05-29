/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.block;

import fr.dev1lroot.mcmods.littlecraft.content.Potty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PottyBlockEntity extends BlockEntity
{
    public static final int MAX_PISS = 1000;
    public static final int MAX_POOP = 1000;
    public static final String TAG_PISS = "piss";
    public static final String TAG_POOP = "poop";

    private int piss = 0;
    private int poop = 0;

    public PottyBlockEntity(BlockPos pos, BlockState state)
    {
        super(Potty.POTTY_BLOCK_ENTITY.get(), pos, state);
    }

    public int getPiss() { return piss; }
    public int getPoop() { return poop; }

    public boolean isPissFull() { return piss >= MAX_PISS; }
    public boolean isPoopFull() { return poop >= MAX_POOP; }

    public void addPiss(int amount)
    {
        piss = Math.min(MAX_PISS, piss + amount);
        setChanged();
    }

    public void addPoop(int amount)
    {
        poop = Math.min(MAX_POOP, poop + amount);
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        output.putInt(TAG_PISS, piss);
        output.putInt(TAG_POOP, poop);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        piss = input.getIntOr(TAG_PISS, 0);
        poop = input.getIntOr(TAG_POOP, 0);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveWithoutMetadata(registries);
    }
}
