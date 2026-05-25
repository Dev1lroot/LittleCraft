package fr.dev1lroot.mcmods.littlecraft.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

// isLittle() was never called externally; LittleData.get(player) now reads the synced attachment directly.
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerLittleMixin {}
