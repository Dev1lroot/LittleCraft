/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.item;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record DiaperFillProperty() implements RangeSelectItemModelProperty
{
    public static final MapCodec<DiaperFillProperty> MAP_CODEC = MapCodec.unit(new DiaperFillProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        int used     = Diaper.getUsed(stack);
        int capacity = Diaper.getCapacity(stack);
        if (used < 10) return 0.0f;
        if (used >= capacity - 100 || used >= 5000) return 1.0f;
        return 0.5f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() { return MAP_CODEC; }
}
