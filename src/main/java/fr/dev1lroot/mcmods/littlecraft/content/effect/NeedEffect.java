/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class NeedEffect extends MobEffect
{
    public NeedEffect()
    {
        // Amber — urgent bodily need
        super(MobEffectCategory.HARMFUL, 0xFF8C00);
    }
}
