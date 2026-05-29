/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.color;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlock;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PottyColorTintSource() implements ItemTintSource
{
    public static final MapCodec<PottyColorTintSource> MAP_CODEC = MapCodec.unit(PottyColorTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner)
    {
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof PottyBlock potty)
            return ARGB.opaque(potty.getColor().getTextureDiffuseColor());
        return 0xFFFFFFFF;
    }

    @Override
    public MapCodec<PottyColorTintSource> type() { return MAP_CODEC; }
}
