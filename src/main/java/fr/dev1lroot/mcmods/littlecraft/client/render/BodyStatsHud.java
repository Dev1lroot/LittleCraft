/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.item.Pacifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.Locale;

public class BodyStatsHud implements GuiLayer
{
    private static final int COLOR_STOMACH = 0xFF8B4513; // saddle brown
    private static final int COLOR_EATEN   = 0xFFF4A460; // sandy brown (pending)
    private static final int COLOR_BLADDER = 0xFFFFFF00; // yellow
    private static final int COLOR_DRINKED = 0xFFADD8E6; // light blue (pending)

    @Override
    public void render(GuiGraphicsExtractor gui, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        Player player = mc.player;
        if (!LittleData.isLittle(player)) return;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(head.getItem() instanceof Pacifier.PacifierItem)) return;
        Component name = head.getCustomName();
        if (name == null || !name.getString().equals("Debug")) return;

        int age        = LittleData.getAge(player);
        int stomach    = LittleData.getStomach(player);
        int stomachCap = LittleData.computeStomachCapacity(age);
        int eaten      = LittleData.getEaten(player);
        int bladder    = LittleData.getBladder(player);
        int bladderCap = LittleData.computeBladderCapacity(age);
        int drinked    = LittleData.getDrinked(player);

        String stomachText = "stomach: "  + format(stomach) + "/" + format(stomachCap) + "g";
        String eatenText   = "eating: "   + format(eaten)   + "g";
        String bladderText = "bladder: "  + format(bladder) + "/" + format(bladderCap) + "ml";
        String drinkedText = "drinking: " + format(drinked) + "ml";

        int lineH  = mc.font.lineHeight + 2;
        int rightX = gui.guiWidth() - 5;
        int y      = gui.guiHeight() - 5 - lineH * 4;

        drawRight(gui, mc, stomachText, rightX, y,           COLOR_STOMACH);
        drawRight(gui, mc, eatenText,   rightX, y + lineH,   COLOR_EATEN);
        drawRight(gui, mc, bladderText, rightX, y + lineH*2, COLOR_BLADDER);
        drawRight(gui, mc, drinkedText, rightX, y + lineH*3, COLOR_DRINKED);
    }

    private static void drawRight(GuiGraphicsExtractor gui, Minecraft mc, String text, int rightX, int y, int color)
    {
        gui.text(mc.font, text, rightX - mc.font.width(text), y, color);
    }

    private static String format(int value)
    {
        return String.format(Locale.ENGLISH, "%,d", value);
    }
}
