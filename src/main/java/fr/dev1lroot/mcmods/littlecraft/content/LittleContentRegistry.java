/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class LittleContentRegistry
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(MODID);

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
        BLOCKS.register(bus);

        // Регистрируем отдельные классы контента
        fr.dev1lroot.mcmods.littlecraft.content.item.Diaper.register();
        // fr.dev1lroot.mcmods.littlecraft.content.block.MyBlock.register();

        LittleMobEffects.register(bus);
        LittlePotions.register(bus);
    }
}
