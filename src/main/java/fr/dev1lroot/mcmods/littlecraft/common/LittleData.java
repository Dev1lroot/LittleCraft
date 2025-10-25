package fr.dev1lroot.mcmods.littlecraft.common;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/*
    LittleData
    ------------
    This is the *core class* that manages all LittleCraft-specific
    player attributes and data access. ♡

    Role:
    - Registers synchronized data parameters (EntityDataAccessor)
      for Little Mode and Little Age.
    - Provides unified getters and setters that handle both
      synced entity data (for runtime) and persistent NBT
      (for saving and reloading).
    - Acts as the single source of truth for a player’s "littleness".

    Registered parameters:
      • LITTLE — boolean: whether the player is currently in Little Mode.
      • LITTLEAGE — int: current "Little Age" value (placeholder for future mechanics).

    Behavior summary:
    - During common setup, we define and register our custom synced fields.
    - These fields are automatically replicated between client and server.
    - When modified, they are also written to persistent player NBT,
      ensuring that Little Mode survives restarts, respawns, and saves.

    In short:
    This class keeps every little heart consistent between sessions,
    dimensions, and even crashes. It’s the backbone of the whole system.
*/

@Mod("littlecraft")
public class LittleData
{
    // Synced attributes used by all players
    public static EntityDataAccessor<Boolean> LITTLE;
    public static EntityDataAccessor<Integer> LITTLEAGE;

    public LittleData(IEventBus modBus)
    {
        // Register setup callback
        modBus.addListener(this::onCommonSetup);
    }

    // Define the synchronized data IDs used by players
    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            LITTLE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
            LITTLEAGE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
        });
    }

    // Set whether the player is in Little Mode
    public static void set(Player player, boolean value)
    {
        player.getEntityData().set(LITTLE, value);
        player.getPersistentData().putBoolean("IsLittle", value);
    }

    // Get the current Little Mode state
    public static boolean get(Player player)
    {
        if (player.getPersistentData().getBoolean("IsLittle")) return true;
        if (player.getEntityData().get(LITTLE)) return true;
        return false;
    }

    // Set player's Little Age
    public static void setAge(Player player, int value)
    {
        player.getEntityData().set(LITTLEAGE, value);
        player.getPersistentData().putInt("LittleAge", value);
    }

    // Get player's Little Age
    public static int getAge(Player player)
    {
        return player.getEntityData().get(LITTLEAGE);
    }
}
