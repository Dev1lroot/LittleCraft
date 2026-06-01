/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

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
    private ChangingTableClientEvents() {}

    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event)
    {
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose(); // always paired with Post popPose

        AvatarRenderState renderState = event.getRenderState();
        if (!isRidingChangingTable(renderState)) return;

        // Suppress the isPassenger bent-leg animation that setupAnim forces on riders.
        renderState.isPassenger = false;

        // Lock the matrix-level Y rotation (bodyRot drives setupRotations' YP transform)
        // and yRot (drives head orientation in setupAnim) to the seat's fixed orientation
        // so the model doesn't rotate with mouse input.
        var level = Minecraft.getInstance().level;
        Entity entity = level.getEntity(renderState.id);
        float seatYRot = ((Player) entity).getVehicle().getYRot();
        renderState.bodyRot = 90f; // Facing to top
        renderState.yRot    = 0f;
        //renderState.xRot    = 180; // Facing to the correct heading;

        // FUCK EULER

        // Scale the offset by the player's render scale so a half-sized little lands
        // at the same position relative to the table surface as a full-sized player.
        float s = renderState.scale;
        poseStack.translate(-0.5F * s, 0.51F * s, 0);

        // Rotate the whole model 90° around Z to go from vertical → horizontal.
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
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
