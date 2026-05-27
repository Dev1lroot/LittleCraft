/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

// Replaced by NeoForge data attachments in LittleData — synced entity data is no longer used.
@Mixin(Player.class)
public abstract class PlayerLittleMixin {}
