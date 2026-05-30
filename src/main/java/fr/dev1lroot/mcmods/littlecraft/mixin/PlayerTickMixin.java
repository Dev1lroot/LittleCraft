/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.LittleMobEffects;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import fr.dev1lroot.mcmods.littlecraft.network.PissPacket;
import fr.dev1lroot.mcmods.littlecraft.network.PoopPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

            int stomachCap = LittleData.computeStomachCapacity(age);
            int bladderCap = LittleData.computeBladderCapacity(age);

            // Drain eaten buffer → stomach: 1 g per 20 ticks.
            // Blocked when stomach is full; signals need instead.
            if (player.tickCount % 20 == 0)
            {
                int eaten = LittleData.getEaten(player);
                if (eaten > 0)
                {
                    if (LittleData.getStomach(player) < stomachCap)
                    {
                        LittleData.addToStomach(player, 1);
                        LittleData.setEaten(player, eaten - 1);
                    }
                    else
                    {
                        player.addEffect(new MobEffectInstance(LittleMobEffects.NEED, 100, 0));
                    }
                }
            }

            // Drain drinked buffer → bladder: 1 ml per 5 ticks.
            // Blocked when bladder is full; signals need instead.
            if (player.tickCount % 5 == 0)
            {
                int drinked = LittleData.getDrinked(player);
                if (drinked > 0)
                {
                    if (LittleData.getBladder(player) < bladderCap)
                    {
                        LittleData.addToBladder(player, 1);
                        LittleData.setDrinked(player, drinked - 1);
                    }
                    else
                    {
                        player.addEffect(new MobEffectInstance(LittleMobEffects.NEED, 100, 0));
                    }
                }
            }

            // While INCONTINENCE is active: involuntary release every 40 ticks through
            // the full routing chain (diaper → pants → potty → floor).
            if (player.hasEffect(LittleMobEffects.INCONTINENCE)
                    && !PissPacket.isActivePissing(player)
                    && player.tickCount % 40 == 0)
            {
                int bladder = LittleData.getBladder(player);
                if (bladder > 0)
                    PissPacket.performPiss(player, Math.max(1, bladder * 24 / bladderCap));

                if (LittleData.getStomach(player) >= stomachCap)
                    PoopPacket.performPoop(player);
            }

            // Age-based involuntary release: every 10 ticks when either reservoir is
            // full and its pending buffer is ≥ half that capacity.
            // Chance: 0–30% for age < 18 (younger = higher); 0% for age 18–40;
            //         0–30% for age > 40 (older = higher, caps at age 70).
            // Uses the same routing as the manual packets (diaper → pants → potty → floor).
            if (player.tickCount % 10 == 0)
            {
                float chance = 0f;
                if (age < 18)
                    chance = 0.30f * (18 - age) / 18f;
                else if (age > 40)
                    chance = Math.min(0.30f, (age - 40) / 100f);
                chance = Math.max(chance, 0.001f);

                if (chance > 0f)
                {
                    boolean bladderOverloaded = LittleData.getBladder(player) >= bladderCap
                            && LittleData.getDrinked(player) >= bladderCap / 2;
                    boolean stomachOverloaded = LittleData.getStomach(player) >= stomachCap
                            && LittleData.getEaten(player) >= stomachCap / 2;

                    if ((bladderOverloaded || stomachOverloaded)
                            && player.getRandom().nextFloat() < chance)
                    {
                        int duration = player.getRandom().nextIntBetweenInclusive(100, 200);
                        player.addEffect(new MobEffectInstance(LittleMobEffects.INCONTINENCE, duration, 0));
                    }
                }
            }
        }

        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);

        // Any leg item marked is_pooped continuously applies stink
        if (player.tickCount % 10 == 0 && !player.level().isClientSide()
                && Diaper.isPooped(legs) && !player.hasEffect(LittleMobEffects.STINK))
        {
            player.addEffect(new MobEffectInstance(LittleMobEffects.STINK, MobEffectInstance.INFINITE_DURATION, 0));
        }

        if (legs.getItem() instanceof Diaper.DiaperItem)
        {
            ItemStack diaper = legs;
            int used     = Diaper.getUsed(diaper);
            int capacity = Diaper.getCapacity(diaper);

            if (player.tickCount % 10 == 0 && !player.level().isClientSide())
            {
                if (used >= 5000)
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
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
