/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractCribBlock;
import fr.dev1lroot.mcmods.littlecraft.content.block.CribBlockEntity;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.EnumMap;
import java.util.Map;

public class Crib
{
    public static final Map<DyeColor, DeferredBlock<AbstractCribBlock>> CRIB_BLOCKS =
        new EnumMap<>(DyeColor.class);

    public static final Map<DyeColor, DeferredItem<BedItem>> CRIB_ITEMS =
        new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            DeferredBlock<AbstractCribBlock> block = LittleContentRegistry.BLOCKS.registerBlock(
                color.getName() + "_crib",
                props -> new AbstractCribBlock(color, props),
                props -> props.mapColor(color.getMapColor()).strength(0.2F).sound(SoundType.WOOL)
            );
            CRIB_BLOCKS.put(color, block);
        }
        for (DyeColor color : DyeColor.values()) {
            CRIB_ITEMS.put(color, LittleContentRegistry.ITEMS.registerItem(
                color.getName() + "_crib",
                props -> new BedItem(CRIB_BLOCKS.get(color).get(), props)
            ));
        }
    }

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CribBlockEntity>> CRIB_BLOCK_ENTITY =
        (DeferredHolder<BlockEntityType<?>, BlockEntityType<CribBlockEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.BLOCK_ENTITY_TYPES.register("crib", () -> {
                AbstractCribBlock[] blocks = CRIB_BLOCKS.values().stream()
                    .map(DeferredBlock::get)
                    .toArray(AbstractCribBlock[]::new);
                return new BlockEntityType<>(CribBlockEntity::new, blocks);
            });

    public static void register() {}
}
