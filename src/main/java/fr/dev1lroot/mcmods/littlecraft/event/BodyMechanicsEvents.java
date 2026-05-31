/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.LittleMobEffects;
import fr.dev1lroot.mcmods.littlecraft.content.effect.IncontinenceEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.NeedEffect;
import fr.dev1lroot.mcmods.littlecraft.content.effect.ShameEffect;
import fr.dev1lroot.mcmods.littlecraft.data.FoodEffectsLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class BodyMechanicsEvents
{
    // Saves the Stink effect instance when milk is about to be drunk, so it can be
    // re-applied after milk would have cleared it. Keyed by entity UUID.
    private static final Map<UUID, MobEffectInstance> savedStinkForMilk = new HashMap<>();

    // Tracks which entities were in water last tick for entry detection.
    private static final Set<UUID> lastInWater = new HashSet<>();

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
    public static void onAddReloadListeners(AddServerReloadListenersEvent event)
    {
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "food_effects"), new FoodEffectsLoader());
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
        Identifier itemId = BuiltInRegistries.ITEM.getKey(event.getItem().getItem());
        FoodEffectsLoader.FoodEffect effect = FoodEffectsLoader.get(itemId);

        if (anim == ItemUseAnimation.EAT)
        {
            if (effect != null)
            {
                LittleData.addToEaten(player, effect.stomach());
                LittleData.addToDrinked(player, effect.bladder());
            }
            else
            {
                LittleData.addToEaten(player, 5);
                LittleData.addToDrinked(player, 10);
            }
        }
        else if (anim == ItemUseAnimation.DRINK)
        {
            if (effect != null)
            {
                LittleData.addToEaten(player, effect.stomach());
                LittleData.addToDrinked(player, effect.bladder());
            }
            else
            {
                LittleData.addToDrinked(player, 250);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        BlockState state = player.level().getBlockState(event.getPos());

        if (!state.is(Blocks.CAKE) && !state.is(BlockTags.CANDLE_CAKES)) return;
        if (!player.canEat(false)) return;

        // All cake variants share minecraft:cake as their food effect key.
        FoodEffectsLoader.FoodEffect effect = FoodEffectsLoader.get(Identifier.withDefaultNamespace("cake"));
        if (effect != null)
        {
            LittleData.addToEaten(player, effect.stomach());
            LittleData.addToDrinked(player, effect.bladder());
        }
        else
        {
            LittleData.addToEaten(player, 5);
            LittleData.addToDrinked(player, 10);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        boolean inWater    = entity.isInWater();
        boolean wasInWater = lastInWater.contains(entity.getUUID());

        if (inWater)
        {
            if (entity.hasEffect(LittleMobEffects.STINK))
                entity.removeEffect(LittleMobEffects.STINK);

            // First tick entering water: flood the diaper without marking is_peed.
            if (!wasInWater && entity instanceof Player player)
            {
                ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
                if (legs.getItem() instanceof Diaper.DiaperItem)
                {
                    int capacity = Diaper.getCapacity(legs);
                    player.setItemSlot(EquipmentSlot.LEGS, Diaper.setUsed(legs, capacity));
                }
            }

            lastInWater.add(entity.getUUID());
        }
        else
        {
            lastInWater.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectParticle(EffectParticleModificationEvent event)
    {
        MobEffect effect = event.getEffect().getEffect().value();
        if (effect instanceof IncontinenceEffect
                || effect instanceof NeedEffect
                || effect instanceof ShameEffect)
            event.setVisible(false);
    }
}
