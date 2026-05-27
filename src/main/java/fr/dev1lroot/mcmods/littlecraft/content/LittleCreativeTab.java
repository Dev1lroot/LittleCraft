/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class LittleCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
        CREATIVE_TABS.register("littlecraft", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.littlecraft"))
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> new ItemStack(Diaper.DIAPER.get()))
            .displayItems((params, output) -> {
                // Diaper
                output.accept(Diaper.DIAPER.get());

                // Cribs — follow DyeColor enum order (white → black)
                for (DyeColor color : DyeColor.values()) {
                    output.accept(Crib.CRIB_ITEMS.get(color).get());
                }

                // Potions of Regression
                output.accept(potionStack(Items.POTION,           LittlePotions.REGRESSION_POTION));
                output.accept(potionStack(Items.SPLASH_POTION,    LittlePotions.REGRESSION_POTION));
                output.accept(potionStack(Items.LINGERING_POTION, LittlePotions.REGRESSION_POTION));
                output.accept(potionStack(Items.TIPPED_ARROW,     LittlePotions.REGRESSION_POTION));

                // Potions of Growth
                output.accept(potionStack(Items.POTION,           LittlePotions.GROWTH_POTION));
                output.accept(potionStack(Items.SPLASH_POTION,    LittlePotions.GROWTH_POTION));
                output.accept(potionStack(Items.LINGERING_POTION, LittlePotions.GROWTH_POTION));
                output.accept(potionStack(Items.TIPPED_ARROW,     LittlePotions.GROWTH_POTION));
            })
            .build()
        );

    private static ItemStack potionStack(net.minecraft.world.item.Item item,
                                         net.neoforged.neoforge.registries.DeferredHolder<
                                             net.minecraft.world.item.alchemy.Potion,
                                             net.minecraft.world.item.alchemy.Potion> potion)
    {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }
}
