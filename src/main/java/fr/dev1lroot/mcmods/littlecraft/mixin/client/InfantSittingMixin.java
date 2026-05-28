/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin.client;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class InfantSittingMixin
{
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("HEAD"))
    private void littlecraft$applyInfantSitting(AvatarRenderState state, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof Player player)) return;

        int age = LittleData.getAge(player);
        if (age <= 0 || age >= 3) return;

        if (state.bedOrientation != null || state.isVisuallySwimming || state.isFallFlying) return;

        // Let vanilla's setupAnim own the sitting pose entirely.
        state.isPassenger = true;
    }
}
