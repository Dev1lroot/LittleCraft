package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

/*
    LocalPlayerLittleMixin
    -----------------------
    This is a *client-only stub* made especially for singleplayer. ♡

    In multiplayer, the "IsLittle" state is synchronized by the server,
    but in singleplayer there is no dedicated server to handle that.
    This mixin makes sure that LocalPlayer (the client-side player)
    can still access its own LittleCraft state correctly.

    Basically: it’s a soft fallback, letting little players stay tiny
    and consistent in singleplayer worlds, without needing server sync. 🧸
*/

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerLittleMixin
{
    // Returns true if the client player is currently in Little Mode
    // Works in singleplayer and local sessions only
    public boolean isLittle()
    {
        LocalPlayer self = (LocalPlayer)(Object)this;

        // Check saved NBTs first
        if (self.getPersistentData().getBoolean("IsLittle"))
            return true;

        // Then check Entity data if possible
        if (self.getEntityData().get(LittleData.LITTLE))
            return true;

        // Otherwise, player is not in Little Mode =(
        return false;
    }
}
