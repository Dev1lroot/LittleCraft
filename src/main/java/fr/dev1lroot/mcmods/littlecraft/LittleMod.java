package fr.dev1lroot.mcmods.littlecraft;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import fr.dev1lroot.mcmods.littlecraft.command.LittleCommand;

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
        // Register all the cute little commands (like /little true/false or /islittle)
        LittleCommand.register();
    }
}
