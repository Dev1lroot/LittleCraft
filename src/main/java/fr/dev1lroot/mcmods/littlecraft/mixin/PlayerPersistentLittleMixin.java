package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    PlayerPersistentLittleMixin
    ----------------------------
    This mixin handles saving and loading LittleCraft data
    into the player’s save file. ♡

    Purpose:
    - Ensures that “Little Mode” and “Little Age” are stored
      inside the world’s player data (NBT) when the game saves.
    - Restores those same values when the player joins or reloads
      their world, keeping everything consistent and cozy.

    In short:
    Without this, little players would forget who they are after
    every restart! This class makes sure they stay small and safe,
    even after naps (aka save/load cycles). 🧸

    Technical notes:
    - Data is stored inside a dedicated "Littlecraft" NBT compound.
    - Only runs on server or integrated singleplayer host.
*/

@Mixin(Player.class)
public abstract class PlayerPersistentLittleMixin
{
    // Save player’s LittleCraft data into their NBT on world save
    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void littlecraft$saveData(CompoundTag tag, CallbackInfo ci)
    {
        Player player = (Player)(Object)this;

        CompoundTag littleTag = new CompoundTag();
        littleTag.putBoolean("IsLittle", LittleData.get(player));
        littleTag.putInt("LittleAge", LittleData.getAge(player));

        tag.put("Littlecraft", littleTag);
    }

    // Load player’s LittleCraft data back from NBT on world load
    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void littlecraft$loadData(CompoundTag tag, CallbackInfo ci)
    {
        Player player = (Player)(Object)this;

        if (tag.contains("Littlecraft"))
        {
            CompoundTag littleTag = tag.getCompound("Littlecraft");
            boolean isLittle = littleTag.getBoolean("IsLittle");
            int littleAge = littleTag.getInt("LittleAge");

            // Restore internal persistent data for the player
            player.getPersistentData().putBoolean("IsLittle", isLittle);
            player.getPersistentData().putInt("LittleAge", littleAge);
        }
    }
}
