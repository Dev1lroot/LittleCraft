/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlock;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlockEntity;
import fr.dev1lroot.mcmods.littlecraft.content.entity.PottySeatEntity;
import fr.dev1lroot.mcmods.littlecraft.content.recipe.PottyClearRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.EnumMap;
import java.util.Map;

public class Potty
{
    public static final Map<DyeColor, DeferredBlock<PottyBlock>> POTTY_BLOCKS =
        new EnumMap<>(DyeColor.class);

    public static final Map<DyeColor, DeferredItem<BlockItem>> POTTY_ITEMS =
        new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            POTTY_BLOCKS.put(color, LittleContentRegistry.BLOCKS.registerBlock(
                color.getName() + "_potty",
                props -> new PottyBlock(color, props),
                props -> props.mapColor(color.getMapColor()).strength(1.0F).sound(SoundType.STONE).noOcclusion()
            ));
        }
        for (DyeColor color : DyeColor.values()) {
            POTTY_ITEMS.put(color, LittleContentRegistry.ITEMS.registerItem(
                color.getName() + "_potty",
                props -> new BlockItem(POTTY_BLOCKS.get(color).get(), props)
            ));
        }
    }

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PottyBlockEntity>> POTTY_BLOCK_ENTITY =
        (DeferredHolder<BlockEntityType<?>, BlockEntityType<PottyBlockEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.BLOCK_ENTITY_TYPES.register("potty", () -> {
                PottyBlock[] blocks = POTTY_BLOCKS.values().stream()
                    .map(DeferredBlock::get)
                    .toArray(PottyBlock[]::new);
                return new BlockEntityType<>(PottyBlockEntity::new, blocks);
            });

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<EntityType<?>, EntityType<PottySeatEntity>> POTTY_SEAT =
        (DeferredHolder<EntityType<?>, EntityType<PottySeatEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.ENTITY_TYPES.register("potty_seat", id ->
                EntityType.Builder.<PottySeatEntity>of(PottySeatEntity::new, MobCategory.MISC)
                    .noSummon()
                    .sized(0.5F, 0.5F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    public static void register()
    {
        PottyClearRecipe.registerSerializer();
    }
}
