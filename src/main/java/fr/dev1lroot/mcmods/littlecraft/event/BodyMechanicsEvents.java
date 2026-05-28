/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.LittleMobEffects;
import fr.dev1lroot.mcmods.littlecraft.content.effect.IncontinenceEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class BodyMechanicsEvents
{
    // Saves the Stink effect instance when milk is about to be drunk, so it can be
    // re-applied after milk would have cleared it. Keyed by entity UUID.
    private static final Map<UUID, MobEffectInstance> savedStinkForMilk = new HashMap<>();

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event)
    {
        if (event.getEntity().level().isClientSide()) return;
        if (!event.getItem().is(Items.MILK_BUCKET)) return;

        LivingEntity entity = event.getEntity();
        MobEffectInstance stink = entity.getEffect(LittleMobEffects.STINK);
        if (stink != null)
            savedStinkForMilk.put(entity.getUUID(), new MobEffectInstance(stink));
    }

    @SubscribeEvent
    public static void onItemUseStop(LivingEntityUseItemEvent.Stop event)
    {
        if (event.getEntity().level().isClientSide()) return;
        if (!event.getItem().is(Items.MILK_BUCKET)) return;
        // Player cancelled drinking — effects were not cleared, discard saved copy.
        savedStinkForMilk.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntity().level().isClientSide()) return;

        // Restore Stink if milk just stripped it.
        if (event.getItem().is(Items.MILK_BUCKET))
        {
            MobEffectInstance saved = savedStinkForMilk.remove(event.getEntity().getUUID());
            if (saved != null && !event.getEntity().hasEffect(LittleMobEffects.STINK))
                event.getEntity().addEffect(new MobEffectInstance(saved));
        }

        if (!(event.getEntity() instanceof Player player)) return;

        ItemUseAnimation anim = event.getItem().getItem().getUseAnimation(event.getItem());

        if (anim == ItemUseAnimation.EAT)
        {
            LittleData.addToStomach(player, 5);
            LittleData.addToBladder(player, 10);
        }
        else if (anim == ItemUseAnimation.DRINK)
        {
            LittleData.addToBladder(player, 250);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;
        if (!entity.isInWater()) return;
        if (entity.hasEffect(LittleMobEffects.STINK))
            entity.removeEffect(LittleMobEffects.STINK);
    }

    @SubscribeEvent
    public static void onEffectParticle(EffectParticleModificationEvent event)
    {
        if (event.getEffect().getEffect().value() instanceof IncontinenceEffect)
            event.setVisible(false);
    }
}
