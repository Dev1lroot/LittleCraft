/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class LittlePotions
{
    public static final DeferredRegister<Potion> POTIONS =
        DeferredRegister.create(Registries.POTION, MODID);

    public static final DeferredHolder<Potion, Potion> REGRESSION_POTION =
        POTIONS.register("regression", () ->
            new Potion("littlecraft.regression",
                new MobEffectInstance(LittleMobEffects.REGRESSION, 1)));

    public static final DeferredHolder<Potion, Potion> GROWTH_POTION =
        POTIONS.register("growth", () ->
            new Potion("littlecraft.growth",
                new MobEffectInstance(LittleMobEffects.GROWTH, 1)));

    public static void register(IEventBus bus)
    {
        POTIONS.register(bus);
    }

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event)
    {
        var builder = event.getBuilder();
        // Awkward Potion + Golden Dandelion = Potion of Regression
        builder.addMix(Potions.AWKWARD, Items.GOLDEN_DANDELION, REGRESSION_POTION);
        // Awkward Potion + Bone Meal = Potion of Growth
        builder.addMix(Potions.AWKWARD, Items.BONE_MEAL, GROWTH_POTION);
    }
}
