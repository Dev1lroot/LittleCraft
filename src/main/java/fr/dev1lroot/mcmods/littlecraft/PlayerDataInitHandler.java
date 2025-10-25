package fr.dev1lroot.mcmods.littlecraft;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/*
    PlayerDataInitHandler
    ---------------------
    This tiny helper makes sure every little player
    feels safe and cozy when they join the world again ♡
    It gently restores their "littleness" and age info,
    from NBT tags attached to them on server to their
    entity model,
    so nobody has to start over after a nap or restart.
*/

@EventBusSubscriber(modid = "littlecraft")
public class PlayerDataInitHandler
{
    // When a player spawns into the world, we listen carefully...
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event)
    {
        // Is it a player? (We only care about our cute littles, not mobs!)
        if (event.getEntity() instanceof Player player)
        {
            // Get the player’s synced data, so we can tuck their state back in
            SynchedEntityData data = player.getEntityData();

            // Bring back their "IsLittle" flag — tiny again, just like before! ✨
            data.set(LittleData.LITTLE, player.getPersistentData().getBoolean("IsLittle"));

            // Restore their LittleAge — because every little has their own special age ♡
            data.set(LittleData.LITTLEAGE, player.getPersistentData().getInt("LittleAge"));
        }
    }
}
