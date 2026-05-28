/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class LittleMaterials
{
    public static final TagKey<Item> FABRIC_TAG = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, "fabric"));

    public static final DeferredItem<Item> FABRIC    = LittleContentRegistry.ITEMS.registerItem("fabric",    Item::new);
    public static final DeferredItem<Item> PLASTIC   = LittleContentRegistry.ITEMS.registerItem("plastic",   Item::new);
    public static final DeferredItem<Item> ABSORBENT = LittleContentRegistry.ITEMS.registerItem("absorbent", Item::new);
    public static final DeferredItem<Item> PADDING   = LittleContentRegistry.ITEMS.registerItem("padding",   Item::new);

    public static void register() {}
}
