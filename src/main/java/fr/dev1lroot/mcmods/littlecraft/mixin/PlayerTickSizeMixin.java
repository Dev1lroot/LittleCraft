package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    PlayerTickSizeMixin
    --------------------
    This mixin ensures that switching between “Little” and “Adult”
    states immediately updates the player’s physical properties. ♡

    Why it’s needed:
    - The visual (render) side updates automatically when the player
      changes state, so they *look* smaller or bigger right away.
    - However, hitbox changes defined in LittleSizeMixin only take
      effect after the game calls refreshDimensions().
      Without this mixin, the hitbox might stay incorrect until
      the next natural dimension refresh (like sneaking).

    What it does:
    - Tracks the last known Little state every tick.
    - When a change is detected, it triggers player.refreshDimensions(),
      forcing the server to immediately recalculate size and collisions.

    In short:
    This class makes sure that when a player grows up or regresses,
    the world around them updates instantly:
    - no waiting, - no desyncs, - no spam refreshing.
*/

@Mixin(Player.class)
public abstract class PlayerTickSizeMixin
{
    private boolean lastLittleState = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void littlecraft$tick(CallbackInfo ci)
    {
        Player player = (Player)(Object)this;
        boolean current = LittleData.get(player);

        // Detect state change between Little and Adult
        if (current != lastLittleState)
        {
            // Force update of player hitbox and physical properties
            player.refreshDimensions();
            lastLittleState = current;
        }
    }
}
