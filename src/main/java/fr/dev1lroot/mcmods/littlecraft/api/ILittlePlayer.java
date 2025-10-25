package fr.dev1lroot.mcmods.littlecraft.api;

/*
    ILittlePlayer
    -------------
    This interface is part of the LittleCraft API and exists
    to keep both client and server in sync when handling
    "Little Mode" for players. ♡

    It allows Minecraft’s player classes (LocalPlayer and ServerPlayer)
    to safely implement and share the same “isLittle” state,
    so that size and behavior changes are consistent across
    singleplayer, LAN, and dedicated servers.

    In simple words: this tiny bridge makes sure every little
    is treated the same, no matter where they are playing. 🧸
*/
public interface ILittlePlayer
{
    // Returns whether the player is currently in Little Mode
    boolean littlecraft$isLittle();

    // Sets the player's Little Mode state (tiny or normal)
    void littlecraft$setLittle(boolean value);
}
