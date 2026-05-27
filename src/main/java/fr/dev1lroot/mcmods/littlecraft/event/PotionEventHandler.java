/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.content.effect.GrowthEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class PotionEventHandler
{
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof AreaEffectCloud cloud)) return;
        if (cloud.level().isClientSide()) return;
        if (cloud.tickCount % 20 != 0) return;

        PotionContents contents = cloud.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return;

        boolean hasGrowth = false;
        for (MobEffectInstance effect : contents.getAllEffects())
        {
            if (effect.getEffect().value() instanceof GrowthEffect)
            {
                hasGrowth = true;
                break;
            }
        }
        if (!hasGrowth) return;

        ServerLevel level = (ServerLevel) cloud.level();
        BlockPos center = cloud.blockPosition();
        int radius = Math.max(1, (int) cloud.getRadius());

        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 1, radius)))
        {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BonemealableBlock bonemeal
                    && bonemeal.isValidBonemealTarget(level, pos, state)
                    && bonemeal.isBonemealSuccess(level, level.getRandom(), pos, state))
            {
                candidates.add(pos.immutable());
            }
        }

        if (!candidates.isEmpty())
        {
            BlockPos chosen = candidates.get(level.getRandom().nextInt(candidates.size()));
            BlockState chosenState = level.getBlockState(chosen);
            ((BonemealableBlock) chosenState.getBlock()).performBonemeal(level, level.getRandom(), chosen, chosenState);
            BoneMealItem.addGrowthParticles(level, chosen, 3);
        }
    }
}
