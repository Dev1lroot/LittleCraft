package fr.dev1lroot.mcmods.littlecraft.common;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/*
    LittleDataSync
    ----------------
    This helper class listens to player lifecycle events to make sure
    LittleCraft’s attributes are always correctly applied. ♡

    Why it exists:
    - Even though LittleData is normally saved and loaded through mixins,
      there are rare moments (respawns, world reloads, death cloning)
      when the synced data might get lost or desynchronized.
    - These events act as a safety net — they reapply the player’s
      LittleCraft state right after those transitions.

    In other words:
    It’s like a gentle reminder for the world to remember who’s little,
    even if they just woke up from a respawn nap. 🧸

    Events handled:
    • PlayerEvent.Clone — when a player respawns after death or dimension change
      → Copies data from the old entity to the new one
    • PlayerEvent.LoadFromFile — when a player is loaded from a world save
      → Restores their Little mode and age from persistent data
*/

@EventBusSubscriber(modid = "littlecraft")
public class LittleDataSync
{
    // Called when a player entity is cloned (e.g., after death or dimension change)
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        // Copy data stored in persistent NBT (world save)
        boolean isLittle = oldPlayer.getPersistentData().getBoolean("IsLittle");
        int littleAge = oldPlayer.getPersistentData().getInt("LittleAge");

        // Apply copied state to the new entity
        LittleData.set(newPlayer, isLittle);
        LittleData.setAge(newPlayer, littleAge);
    }

    // Called when a player is loaded from their save file
    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event)
    {
        Player player = event.getEntity();

        // Load saved state from persistent data
        boolean isLittle = player.getPersistentData().getBoolean("IsLittle");
        int littleAge = player.getPersistentData().getInt("LittleAge");

        // Apply the values to synced entity data
        LittleData.set(player, isLittle);
        LittleData.setAge(player, littleAge);
    }
}
