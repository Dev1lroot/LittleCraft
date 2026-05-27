/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class LittlePlayerRenderer
{
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event)
    {
        AvatarRenderState renderState = event.getRenderState();

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(renderState.id);
        if (!(entity instanceof Player player)) return;

        if (LittleData.isLittle(player))
        {
            int age = LittleData.getAge(player);
            float bodyScale = LittleData.computeBodyScale(age);
            renderScaled(event, renderState, bodyScale);
        }
        else
        {
            event.getPoseStack().pushPose();
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event)
    {
        event.getPoseStack().popPose();
    }

    private static final float LITTLE_NAMETAG_Y = 2.8F;
    private static final float ADULT_NAMETAG_Y = 2.0F;

    private static void renderScaled(RenderPlayerEvent.Pre<?> event, AvatarRenderState state, float bodyScale)
    {
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.scale(bodyScale, bodyScale, bodyScale);

        if (bodyScale < 1.0F && state.nameTagAttachment != null)
        {
            float t = (bodyScale - 0.5F) / 0.5F; // 0 at little scale, 1 at adult scale
            float nametagY = LITTLE_NAMETAG_Y + (ADULT_NAMETAG_Y - LITTLE_NAMETAG_Y) * t;
            state.nameTagAttachment = new Vec3(state.nameTagAttachment.x, nametagY, state.nameTagAttachment.z);
        }
    }
}
