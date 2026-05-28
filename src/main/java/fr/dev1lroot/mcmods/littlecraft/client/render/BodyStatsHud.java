/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.Locale;

public class BodyStatsHud implements GuiLayer
{
    private static final int COLOR_BLADDER = 0xFFFFFF00; // yellow
    private static final int COLOR_STOMACH = 0xFF8B4513; // brown

    @Override
    public void render(GuiGraphicsExtractor gui, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        Player player = mc.player;
        if (!LittleData.isLittle(player)) return;

        int age      = LittleData.getAge(player);
        int bladder  = LittleData.getBladder(player);
        int bladderCap = LittleData.computeBladderCapacity(age);
        int stomach  = LittleData.getStomach(player);
        int stomachCap = LittleData.computeStomachCapacity(age);

        String bladderText  = format(bladder)  + "/" + format(bladderCap)  + "ml";
        String stomachText  = format(stomach)  + "/" + format(stomachCap)  + "g";

        int x = ((gui.guiWidth() / 2) - 90);
        int y = gui.guiHeight() - 50;

        gui.text(mc.font, bladderText, x, y,      COLOR_BLADDER);
        gui.text(mc.font, stomachText, x + 100, y, COLOR_STOMACH);
    }

    private static String format(int value)
    {
        return String.format(Locale.ENGLISH, "%,d", value);
    }
}
