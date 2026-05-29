/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.color;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.item.Pacifier;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PacifierBodyColorTint() implements ItemTintSource
{
    public static final MapCodec<PacifierBodyColorTint> MAP_CODEC = MapCodec.unit(PacifierBodyColorTint::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner)
    {
        return ARGB.opaque(Pacifier.getBodyColor(stack));
    }

    @Override
    public MapCodec<PacifierBodyColorTint> type() { return MAP_CODEC; }
}
