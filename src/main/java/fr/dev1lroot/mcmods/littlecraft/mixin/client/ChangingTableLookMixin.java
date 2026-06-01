/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin.client;

import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class ChangingTableLookMixin
{
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void littlecraft$lockYawOnTable(double yaw, double pitch, CallbackInfo ci)
    {
        Entity self = (Entity)(Object)this;
        if (!(self instanceof LocalPlayer)) return;
        if (!(self.getVehicle() instanceof ChangingTableSeatEntity)) return;

        ci.cancel();
        float pitchDelta = (float)pitch * 0.15F;
        self.setXRot(Mth.clamp(self.getXRot() + pitchDelta, -90.0F, 90.0F));
        self.xRotO = Mth.clamp(self.xRotO + pitchDelta, -90.0F, 90.0F);
    }
}
