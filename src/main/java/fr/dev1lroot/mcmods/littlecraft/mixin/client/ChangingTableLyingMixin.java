/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin.client;

import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the relaxed lying-down limb spread after all vanilla setupAnim logic,
 * so it runs regardless of when the deferred renderer calls setupAnim.
 */
@Mixin(PlayerModel.class)
public class ChangingTableLyingMixin
{
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("TAIL"))
    private void littlecraft$applyLyingLimbs(AvatarRenderState state, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof Player player)) return;
        if (!(player.getVehicle() instanceof ChangingTableSeatEntity)) return;

        HumanoidModel<AvatarRenderState> model = (HumanoidModel<AvatarRenderState>)(Object)this;

        // Legs: straight and slightly spread
        model.rightLeg.xRot =  0f;
        model.leftLeg.xRot  =  0f;
        model.rightLeg.yRot =  0f;
        model.leftLeg.yRot  =  0f;
        model.rightLeg.zRot =  0.25f;
        model.leftLeg.zRot  = -0.25f;

        // Arms: out to the sides with a slight forward droop
        model.rightArm.xRot = -0.2f;
        model.leftArm.xRot  = -0.2f;
        model.rightArm.zRot = -0.6f;
        model.leftArm.zRot  =  0.6f;
    }
}
