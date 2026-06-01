/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.command;

import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/*
    DiaperCommand
    ----------------
    Registers the /diaper command family:
      /diaper open              — open your own diaper
      /diaper close             — close your own diaper
      /diaper <player> open     — open another player's diaper (gamemaster)
      /diaper <player> close    — close another player's diaper (gamemaster)
*/

public class DiaperCommand
{
    public static void register()
    {
        NeoForge.EVENT_BUS.addListener(DiaperCommand::onRegister);
    }

    private static void onRegister(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(
                Commands.literal("diaper")
                        // /diaper open
                        .then(Commands.literal("open")
                                .executes(ctx -> applyOpen(
                                        ctx.getSource().getPlayerOrException(), true, ctx.getSource()))
                        )
                        // /diaper close
                        .then(Commands.literal("close")
                                .executes(ctx -> applyOpen(
                                        ctx.getSource().getPlayerOrException(), false, ctx.getSource()))
                        )
                        // /diaper <player> open|close  (gamemaster only)
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.literal("open")
                                        .executes(ctx -> applyOpen(
                                                EntityArgument.getPlayer(ctx, "player"), true, ctx.getSource()))
                                )
                                .then(Commands.literal("close")
                                        .executes(ctx -> applyOpen(
                                                EntityArgument.getPlayer(ctx, "player"), false, ctx.getSource()))
                                )
                        )
        );
    }

    private static int applyOpen(ServerPlayer target, boolean open, CommandSourceStack source)
    {
        ItemStack legs = target.getItemBySlot(EquipmentSlot.LEGS);
        if (!(legs.getItem() instanceof Diaper.DiaperItem))
        {
            source.sendFailure(Component.translatable(
                    "littlecraft.command.diaper.no_diaper", target.getDisplayName()));
            return 0;
        }

        target.setItemSlot(EquipmentSlot.LEGS, Diaper.setOpen(legs, open));

        boolean isSelf = source.getEntity() == target;
        if (isSelf)
        {
            source.sendSuccess(() -> Component.translatable(
                    open ? "littlecraft.command.diaper.open.self"
                         : "littlecraft.command.diaper.close.self"), false);
        }
        else
        {
            source.sendSuccess(() -> Component.translatable(
                    open ? "littlecraft.command.diaper.open.other"
                         : "littlecraft.command.diaper.close.other",
                    target.getDisplayName()), true);
            target.sendSystemMessage(Component.translatable(
                    open ? "littlecraft.command.diaper.open.notified"
                         : "littlecraft.command.diaper.close.notified"));
        }
        return 1;
    }
}
