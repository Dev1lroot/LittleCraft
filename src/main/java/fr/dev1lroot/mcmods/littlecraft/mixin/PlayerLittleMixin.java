package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    PlayerLittleMixin
    -----------------
    This mixin extends the base Player entity with extra
    LittleCraft attributes. ♡

    It defines two synchronized data fields:
      - LITTLE: whether the player is currently in Little Mode
      - LITTLEAGE: a placeholder for age-related behavior
                   (feature still in development)

    These values are registered with sensible defaults:
      LITTLE = false (normal player)
      LITTLEAGE = 3 (temporary value for testing)

    The purpose of this mixin is to give the Player entity
    the ability to carry mod-specific state across both
    client and server, enabling consistent “Little” behavior
    everywhere — even after respawns or reconnects. 🧸
*/

@Mixin(Player.class)
public abstract class PlayerLittleMixin
{
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void littlecraft$defineData(SynchedEntityData.Builder builder, CallbackInfo ci)
    {
        // Add mod-specific synced data with default values
        builder.define(LittleData.LITTLE, false);
        builder.define(LittleData.LITTLEAGE, 3); // Placeholder until age system is complete
    }
}
