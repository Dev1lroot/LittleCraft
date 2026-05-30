/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content;

import fr.dev1lroot.mcmods.littlecraft.content.effect.GrowthEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.IncontinenceEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.NeedEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.RegressionEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.ShameEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.StinkEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class LittleMobEffects
{
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, MODID);

    public static final DeferredHolder<MobEffect, RegressionEffect> REGRESSION =
        MOB_EFFECTS.register("regression", RegressionEffect::new);

    public static final DeferredHolder<MobEffect, GrowthEffect> GROWTH =
        MOB_EFFECTS.register("growth", GrowthEffect::new);

    public static final DeferredHolder<MobEffect, StinkEffect> STINK =
        MOB_EFFECTS.register("stink", StinkEffect::new);

    public static final DeferredHolder<MobEffect, IncontinenceEffect> INCONTINENCE =
        MOB_EFFECTS.register("incontinence", IncontinenceEffect::new);

    public static final DeferredHolder<MobEffect, NeedEffect> NEED =
        MOB_EFFECTS.register("need", NeedEffect::new);

    public static final DeferredHolder<MobEffect, ShameEffect> SHAME =
        MOB_EFFECTS.register("shame", ShameEffect::new);

    public static void register(IEventBus bus)
    {
        MOB_EFFECTS.register(bus);
    }
}
