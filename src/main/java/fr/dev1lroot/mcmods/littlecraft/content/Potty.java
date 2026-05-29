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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class Potty
{
    public static final DeferredBlock<PottyBlock> POTTY_BLOCK =
        LittleContentRegistry.BLOCKS.registerBlock(
            "potty",
            PottyBlock::new,
            props -> props.strength(1.0F).sound(SoundType.STONE).noOcclusion()
        );

    public static final DeferredItem<BlockItem> POTTY_ITEM =
        LittleContentRegistry.ITEMS.registerItem(
            "potty",
            props -> new BlockItem(POTTY_BLOCK.get(), props)
        );

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PottyBlockEntity>> POTTY_BLOCK_ENTITY =
        (DeferredHolder<BlockEntityType<?>, BlockEntityType<PottyBlockEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.BLOCK_ENTITY_TYPES.register("potty", () ->
                new BlockEntityType<>(PottyBlockEntity::new, POTTY_BLOCK.get())
            );

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
