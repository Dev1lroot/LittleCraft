/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.effect;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class GrowthEffect extends MobEffect
{
    public GrowthEffect()
    {
        super(MobEffectCategory.BENEFICIAL, 0x44BB22);
    }

    @Override
    public boolean isInstantenous()
    {
        return true;
    }

    @Override
    public void applyInstantenousEffect(ServerLevel level, Entity source, Entity indirectSource, LivingEntity entity, int amplifier, double health)
    {
        if (entity instanceof ServerPlayer player)
        {
            int newAge = LittleData.getAge(player) + 1;
            LittleData.setAge(player, newAge);
            player.sendSystemMessage(Component.translatable("littlecraft.notification.potion.growth.self", newAge));
        }
        else if (entity instanceof AgeableMob mob && mob.isBaby())
        {
            mob.setBaby(false);
        }
        else if (entity instanceof Zombie zombie && zombie.isBaby())
        {
            zombie.setBaby(false);
        }
    }
}
