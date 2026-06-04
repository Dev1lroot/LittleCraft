/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Renders the player lying flat when seated on a {@link ChangingTableSeatEntity}.
 *
 * Strategy (following the LittlePlayerRenderer pattern):
 *  Pre  – push the poseStack; if riding a changing table, apply a 90° Z-axis
 *          rotation and clear the isPassenger flag that would bend the legs.
 *  Post – restore model-part rotations for straight limbs; always pop.
 *
 * Registered manually from {@link fr.dev1lroot.mcmods.littlecraft.client.ClientRegistry}
 * via {@code NeoForge.EVENT_BUS.addListener()} to guarantee registration.
 */
public final class ChangingTableClientEvents
{
    private static CameraType savedCamera = null;

    private ChangingTableClientEvents() {}

    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event)
    {
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose(); // always paired with Post popPose

        AvatarRenderState renderState = event.getRenderState();
        if (!isRidingChangingTable(renderState)) return;

        // Suppress the isPassenger bent-leg animation that setupAnim forces on riders.
        renderState.isPassenger = false;

        var level = Minecraft.getInstance().level;
        Entity entity = level.getEntity(renderState.id);
        Player player = (Player) entity;

        // LittlePlayerRenderer fires after us and calls poseStack.scale(bs), which shrinks
        // the model toward its origin (entity feet) AFTER our translate/rotate.  Two axes drift:
        //   • vertical  – model back (model -Z → world -Y) rises by halfBodyDepth*(1-bs)
        //   • heading   – body centre (model +Y → heading) retreats toward foot by halfModelHeight*(1-bs)
        // Pre-compensating by the same amounts keeps both at a fixed world position for any bs.
        // At bs == 1.0 (age ≥ 18) no shrinking occurs; fall back to the renderState.scale-based
        // values that were confirmed correct at full size.
        float bs              = LittleData.computeBodyScale(LittleData.getAge(player));
        float s               = renderState.scale;
        float offsetY         = 0.1f;                 // Y above entity for a full-scale player
        float offsetX         = 0.76f;                // heading offset for a full-scale player
        float halfBodyDepth   = 0.125f*2;                // 2 px = 0.125 blocks (half of 4-px body depth)
        float halfModelHeight = 0.9f;                  // 0.9 blocks (half of 1.8-block model height)
        float shrink          = 1.0f - bs;
        float dy = (bs < 1.0f) ? (offsetY - halfBodyDepth   * shrink) - 0.55f : (offsetY * s);
        float dw = (bs < 1.0f) ? (offsetX - halfModelHeight * shrink) : (offsetX * s);

        // heading = table's FACING direction (feet → head).
        Direction heading = Direction.fromYRot(player.getVehicle().getYRot()).getOpposite();

        switch (heading)
        {
            case EAST -> {
                renderState.bodyRot = 90f;
                renderState.yRot    = 0f;
                poseStack.translate(-dw, dy, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
            }
            case WEST -> {
                renderState.bodyRot = 270f;
                renderState.yRot    = 0f;
                poseStack.translate(dw, dy, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
            }
            case NORTH -> {
                renderState.bodyRot = 0f;
                renderState.yRot    = 0f;
                poseStack.translate(0, dy, dw);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            }
            case SOUTH -> {
                renderState.bodyRot = 180f;
                renderState.yRot    = 0f;
                poseStack.translate(0, dy, -dw);
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            }
            default -> {
                renderState.bodyRot = 90f;
                renderState.yRot    = 0f;
                poseStack.translate(-dw, dy, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
            }
        }
    }

    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event)
    {
        AvatarRenderState renderState = event.getRenderState();

        // Post fires after setupAnim – forcibly straighten the legs regardless of
        // whether the isPassenger suppression above was sufficient.
        if (isRidingChangingTable(renderState))
        {
            var model = event.getRenderer().getModel();
            model.rightLeg.xRot =  0f;
            model.leftLeg.xRot  =  0f;
            model.rightLeg.yRot =  0f;
            model.leftLeg.yRot  =  0f;
            model.rightLeg.zRot =  0.5f;
            model.leftLeg.zRot  = -0.5f;
            model.rightArm.xRot = 0.2f;
            model.leftArm.xRot  = 0.2f;
            model.rightArm.zRot = 0.6f;
            model.leftArm.zRot  = -0.6f;
        }

        event.getPoseStack().popPose(); // always paired with Pre pushPose
    }

    public static void onClientLevelTick(LevelTickEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean onTable = mc.player.getVehicle() instanceof ChangingTableSeatEntity;

        if (onTable && savedCamera == null && mc.options.getCameraType().isFirstPerson())
        {
            savedCamera = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        else if (!onTable && savedCamera != null)
        {
            mc.options.setCameraType(savedCamera);
            savedCamera = null;
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static boolean isRidingChangingTable(AvatarRenderState renderState)
    {
        var level = Minecraft.getInstance().level;
        if (level == null) return false;

        Entity entity = level.getEntity(renderState.id);
        if (!(entity instanceof Player player)) return false;

        return player.getVehicle() instanceof ChangingTableSeatEntity;
    }
}
