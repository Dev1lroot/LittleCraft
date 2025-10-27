package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    PlayerTickMixin
    --------------------
    Imagine a tiny magical gremlin sitting on the player's shoulder.
    Every single tick, this mixin lets that gremlin:
    1) Check if the player is "Little" or "Adult".
    2) If the player suddenly shrinks or grows, it yells:
       "HEY MINECRAFT, UPDATE THE HITBOX!!!"
    3) Then it sneaks a peek at the player's legs to see if
       they're wearing a diaper.
    4) If yes... the gremlin pokes it every 2 seconds to make it
       a little bit soggier, because that’s how life works.

    In short:
    - Keeps the player’s size accurate (no cursed invisible hitboxes).
    - Slowly wears down any diaper being worn,
      because even pixel diapers can’t last forever.
*/

@Mixin(Player.class)
public abstract class PlayerTickMixin
{
    // Remember if the player was smol last tick.
    private boolean lastLittleState = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void littlecraft$tick(CallbackInfo ci)
    {
        Player player = (Player)(Object)this;
        boolean current = LittleData.get(player);

        // Step 1: Did the player shrink or grow?
        if (current != lastLittleState)
        {
            // Yes! Force update the hitbox or risk cursed collisions.
            player.refreshDimensions();
            lastLittleState = current;
        }

        // Step 2: Is the player wearing a diaper on their precious legs?
        ItemStack diaper = player.getItemBySlot(EquipmentSlot.LEGS);
        if (diaper.getItem() instanceof Diaper.DiaperItem)
        {
            // Every 40 ticks (2 seconds) the diaper takes one "usage bonk".
            if (player.tickCount % 40 == 0)
            {
                // But don’t fully destroy it - no exploding diapers, please.
                if(diaper.getMaxDamage() - 1 > diaper.getDamageValue())
                {
                    // Add one more soggy point.
                    diaper.setDamageValue(diaper.getDamageValue() + 1);
                }
            }
        }
    }
}
