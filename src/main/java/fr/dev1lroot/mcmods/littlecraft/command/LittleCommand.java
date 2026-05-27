/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/*
    LittleCommand
    ----------------
    Registers the /age command family:
      /age get                — show your own little age (everyone)
      /age set <n>            — set your own little age (cheats)
      /age <player> get       — show another player’s little age (everyone)
      /age <player> set <n>   — set another player’s little age (cheats)

    Little Mode is determined purely by age > 0; no separate boolean flag exists.
*/

public class LittleCommand
{
    public static void register()
    {
        NeoForge.EVENT_BUS.addListener(LittleCommand::onRegister);
    }

    private static void onRegister(RegisterCommandsEvent event)
    {
        // Age command — /age get|set and /age <player> get|set <number>
        event.getDispatcher().register(
                Commands.literal("age")
                        // /age get
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int age = LittleData.getAge(player);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("littlecraft.command.getage.self", age), false
                                    );
                                    return age;
                                })
                        )
                        // /age set <number>
                        .then(Commands.literal("set")
                                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("age", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int age = IntegerArgumentType.getInteger(ctx, "age");
                                            LittleData.setAge(player, age);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable("littlecraft.command.setage.self", age), false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        // /age <player> get|set <number>
                        .then(Commands.argument("player", EntityArgument.player())
                                // /age <player> get
                                .then(Commands.literal("get")
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int age = LittleData.getAge(target);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable("littlecraft.command.getage.other", target.getDisplayName(), age), false
                                            );
                                            return age;
                                        })
                                )
                                // /age <player> set <number>
                                .then(Commands.literal("set")
                                        .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                        .then(Commands.argument("age_value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    int age = IntegerArgumentType.getInteger(ctx, "age_value");
                                                    LittleData.setAge(target, age);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("littlecraft.command.setage.other", target.getDisplayName(), age), false
                                                    );
                                                    target.sendSystemMessage(
                                                            Component.translatable("littlecraft.command.setage.notified", age)
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }
}
