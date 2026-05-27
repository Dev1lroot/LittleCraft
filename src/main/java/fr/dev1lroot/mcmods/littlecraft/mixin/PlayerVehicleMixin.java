/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Entity.startRiding hard-rejects vehicles whose EntityType.canSerialize() returns false.
// EntityType.PLAYER uses .noSave(), so canSerialize() is always false, blocking player-as-vehicle.
// This redirect makes the check pass for player vehicles, allowing the carry mechanic.
@Mixin(Entity.class)
public abstract class PlayerVehicleMixin
{
    @Redirect(
        method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z")
    )
    private boolean littlecraft$allowPlayerVehicle(EntityType<?> type)
    {
        return type == EntityType.PLAYER || type.canSerialize();
    }
}
