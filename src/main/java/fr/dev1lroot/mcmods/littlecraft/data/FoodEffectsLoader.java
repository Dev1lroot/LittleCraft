/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * Loads per-food bladder/stomach values from data/<namespace>/littlecraft/food_effects/<item>.json.
 * The file's namespace+path becomes the item Identifier used for lookup (e.g.
 * data/minecraft/littlecraft/food_effects/apple.json → minecraft:apple).
 * Other mods can add their own files under data/<modid>/littlecraft/food_effects/ to register
 * custom food effects without depending on LittleCraft at compile time.
 */
public class FoodEffectsLoader extends SimpleJsonResourceReloadListener<FoodEffectsLoader.FoodEffect>
{
    public record FoodEffect(int stomach, int bladder)
    {
        public static final Codec<FoodEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.optionalFieldOf("stomach", 0).forGetter(FoodEffect::stomach),
                Codec.INT.optionalFieldOf("bladder", 0).forGetter(FoodEffect::bladder)
            ).apply(instance, FoodEffect::new)
        );
    }

    private static volatile Map<Identifier, FoodEffect> REGISTRY = Map.of();

    public FoodEffectsLoader()
    {
        super(FoodEffect.CODEC, FileToIdConverter.json("littlecraft/food_effects"));
    }

    @Override
    protected void apply(Map<Identifier, FoodEffect> objects, ResourceManager manager, ProfilerFiller profiler)
    {
        REGISTRY = Map.copyOf(objects);
    }

    /** Returns the FoodEffect for the given item/block Identifier, or null if not registered. */
    public static FoodEffect get(Identifier id)
    {
        return REGISTRY.get(id);
    }
}
