/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import fr.dev1lroot.mcmods.littlecraft.content.block.CribBlock;
import fr.dev1lroot.mcmods.littlecraft.content.block.CribBlockEntity;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class Crib
{
    public static final DeferredBlock<CribBlock> CRIB_BLOCK =
        LittleContentRegistry.BLOCKS.registerBlock("crib", CribBlock::new,
            props -> props.mapColor(MapColor.WOOL).strength(0.2F).sound(SoundType.WOOL));

    public static final DeferredItem<BedItem> CRIB_ITEM =
        LittleContentRegistry.ITEMS.registerItem("crib",
            props -> new BedItem(CRIB_BLOCK.get(), props));

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CribBlockEntity>> CRIB_BLOCK_ENTITY =
        (DeferredHolder<BlockEntityType<?>, BlockEntityType<CribBlockEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.BLOCK_ENTITY_TYPES.register("crib", () ->
                new BlockEntityType<>(CribBlockEntity::new, CRIB_BLOCK.get()));

    public static void register() {}
}
