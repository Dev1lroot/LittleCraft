/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class StinkEffect extends MobEffect
{
    public StinkEffect()
    {
        // Dark olive green-brown
        super(MobEffectCategory.HARMFUL, 0x556B2F);
    }
}
