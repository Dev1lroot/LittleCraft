/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public record DiaperDesignProperty() implements SelectItemModelProperty<String>
{
    public static final MapCodec<DiaperDesignProperty> MAP_CODEC = MapCodec.unit(new DiaperDesignProperty());
    public static final Type<DiaperDesignProperty, String> TYPE =
        SelectItemModelProperty.Type.create(MAP_CODEC, Codec.STRING);

    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity,
                       int seed, ItemDisplayContext context)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "default";
        return data.copyTag().getStringOr("DESIGN", "default").toLowerCase(Locale.ROOT);
    }

    @Override
    public Codec<String> valueCodec() { return Codec.STRING; }

    @Override
    public Type<DiaperDesignProperty, String> type() { return TYPE; }
}
