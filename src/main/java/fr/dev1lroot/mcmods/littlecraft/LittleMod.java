/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft;

import fr.dev1lroot.mcmods.littlecraft.command.LittleCommand;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.network.PissPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/*
    ╔════════════════════════════════════════════════════════════════╗
    ║                        LittleCraft Mod ♡                       ║
    ║                                                                ║
    ║  Developer: David von Eichendorf (aka dev1lroot / Davy)        ║
    ║  Project: LittleCraft - A cozy Agere/ABDL inspired Minecraft   ║
    ║            mod that adds "Little Mode", gentle immersion,      ║
    ║            and warm roleplay elements for regressors and       ║
    ║            caregivers alike.                                   ║
    ║                                                                ║
    ║  Main class: LittleMod                                         ║
    ║  Purpose: Initializes the mod, registers all commands,         ║
    ║            and sets up a soft, safe foundation for little      ║
    ║            experiences in Minecraft.                           ║
    ║                                                                ║
    ║  License: MIT (or your choice — feel free to adjust)           ║
    ║  Created with love, softness, and too many warm blankets. 🧸   ║
    ╚════════════════════════════════════════════════════════════════╝
*/

@Mod(LittleMod.MODID)
public class LittleMod
{
    // The unique ID for this mod, used by NeoForge for registry and config
    public static final String MODID = "littlecraft";

    /*
        Constructor
        -----------
        Called when the mod is first loaded by NeoForge.
    */
    public LittleMod(IEventBus modEventBus)
    {
        // Register age commands (/age get|set, /age <player> get|set)
        LittleCommand.register();
        // Register all the cute little items and blocks
        LittleContentRegistry.register(modEventBus);
        // Register network payloads
        modEventBus.addListener(LittleMod::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
    {
        event.registrar("1")
                .playToServer(PissPacket.TYPE, PissPacket.STREAM_CODEC, PissPacket::handle);
    }
}
