/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import fr.dev1lroot.mcmods.littlecraft.network.PissPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerTickMixin
{
    private int lastLittleAge  = -1;
    private int lastFoodLevel  = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void littlecraft$tick(CallbackInfo ci)
    {
        Player player = (Player)(Object)this;
        int age = LittleData.getAge(player);

        // Refresh hitbox + camera (eyeHeight = 0.85 * scaled height) whenever age changes.
        if (age != lastLittleAge)
        {
            player.refreshDimensions();
            lastLittleAge = age;
        }

        if (!player.level().isClientSide())
        {
            int foodLevel = player.getFoodData().getFoodLevel();
            if (lastFoodLevel != -1 && foodLevel < lastFoodLevel)
            {
                int decrease = lastFoodLevel - foodLevel;
                LittleData.addToStomach(player, decrease * 10);
                LittleData.addToBladder(player, decrease * 30);
            }
            lastFoodLevel = foodLevel;
        }

        ItemStack diaper = player.getItemBySlot(EquipmentSlot.LEGS);
        if (diaper.getItem() instanceof Diaper.DiaperItem)
        {
            int used     = Diaper.getUsed(diaper);
            int capacity = Diaper.getCapacity(diaper);

            if (!PissPacket.isActivePissing(player) && player.tickCount % 40 == 0 && used < capacity)
            {
                int bladder = LittleData.getBladder(player);
                if (bladder > 0)
                {
                    player.setItemSlot(EquipmentSlot.LEGS, Diaper.setUsed(diaper, used + 1));
                    LittleData.setBladder(player, bladder - 1);
                }
            }

            if (used >= capacity - 200 && player.tickCount % 10 == 0
                    && player.level() instanceof ServerLevel serverLevel)
            {
                serverLevel.sendParticles(
                        ParticleTypes.FALLING_HONEY,
                        player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.4,
                        player.getY() + 0.8,
                        player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.4,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
