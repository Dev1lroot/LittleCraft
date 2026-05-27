/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Override where the little passenger sits on the carrier.
// Default PASSENGER attachment on a player entity falls back to AT_HEIGHT = (0, 1.8, 0),
// which places the rider on top of the carrier's head.
// We want the little player held out in front of the carrier at arm/chest height.
@Mixin(Entity.class)
public abstract class PlayerCarryPositionMixin
{
    @Inject(
        method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void littlecraft$holdInArms(Entity passenger, Entity.MoveFunction moveFunction, CallbackInfo ci)
    {
        Entity vehicle = (Entity)(Object)this;
        if (!(vehicle instanceof Player carrier)) return;
        if (!(passenger instanceof Player little)) return;

        int carrierAge = LittleData.getAge(carrier);
        int littleAge  = LittleData.getAge(little);
        if (carrierAge < 18) return;
        if (littleAge < 1 || littleAge > 12) return;

        // Forward vector from carrier's yaw only (ignore pitch so height stays constant)
        float yawRad = (float) Math.toRadians(carrier.getYRot());
        double fwX = -Math.sin(yawRad);
        double fwZ =  Math.cos(yawRad);

        // Place little player's feet at carrier's chest height, 0.5 blocks forward
        double x = carrier.getX() + fwX * 0.5;
        double y = carrier.getY() + 0.9;
        double z = carrier.getZ() + fwZ * 0.5;

        moveFunction.accept(little, x, y, z);
        ci.cancel();
    }
}
