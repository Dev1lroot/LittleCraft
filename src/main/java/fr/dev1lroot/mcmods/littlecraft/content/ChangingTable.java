/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import fr.dev1lroot.mcmods.littlecraft.content.block.ChangingTableBlockEntity;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.EnumMap;
import java.util.Map;

public class ChangingTable
{
    public static final Map<DyeColor, DeferredBlock<AbstractChangingTableBlock>> CHANGING_TABLE_BLOCKS =
        new EnumMap<>(DyeColor.class);

    public static final Map<DyeColor, DeferredItem<BedItem>> CHANGING_TABLE_ITEMS =
        new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            DeferredBlock<AbstractChangingTableBlock> block = LittleContentRegistry.BLOCKS.registerBlock(
                color.getName() + "_changing_table",
                props -> new AbstractChangingTableBlock(color, props),
                props -> props.mapColor(color.getMapColor()).strength(0.2F).sound(SoundType.WOOL)
            );
            CHANGING_TABLE_BLOCKS.put(color, block);
        }
        for (DyeColor color : DyeColor.values()) {
            CHANGING_TABLE_ITEMS.put(color, LittleContentRegistry.ITEMS.registerItem(
                color.getName() + "_changing_table",
                props -> new BedItem(CHANGING_TABLE_BLOCKS.get(color).get(), props)
            ));
        }
    }

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChangingTableBlockEntity>> CHANGING_TABLE_BLOCK_ENTITY =
        (DeferredHolder<BlockEntityType<?>, BlockEntityType<ChangingTableBlockEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.BLOCK_ENTITY_TYPES.register("changing_table", () -> {
                AbstractChangingTableBlock[] blocks = CHANGING_TABLE_BLOCKS.values().stream()
                    .map(DeferredBlock::get)
                    .toArray(AbstractChangingTableBlock[]::new);
                return new BlockEntityType<>(ChangingTableBlockEntity::new, blocks);
            });

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<EntityType<?>, EntityType<ChangingTableSeatEntity>> CHANGING_TABLE_SEAT =
        (DeferredHolder<EntityType<?>, EntityType<ChangingTableSeatEntity>>)
            (DeferredHolder<?, ?>) LittleContentRegistry.ENTITY_TYPES.register("changing_table_seat", id ->
                EntityType.Builder.<ChangingTableSeatEntity>of(ChangingTableSeatEntity::new, MobCategory.MISC)
                    .noSummon()
                    .sized(0.5F, 0.5F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    public static void register() {}
}
