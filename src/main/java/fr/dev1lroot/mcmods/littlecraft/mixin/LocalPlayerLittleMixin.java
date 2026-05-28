/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerLittleMixin
{
    @Shadow public ClientInput input;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void littlecraft$blockInfantMovement(CallbackInfo ci)
    {
        Player player = (Player)(Object) this;
        int age = LittleData.getAge(player);
        if (age <= 0 || age >= 3) return;

        Input old = this.input.keyPresses;
        this.input.keyPresses = new Input(false, false, false, false, old.jump(), old.shift(), old.sprint());
    }
}
