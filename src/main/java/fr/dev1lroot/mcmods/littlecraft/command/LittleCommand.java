package fr.dev1lroot.mcmods.littlecraft.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

/*
    LittleCommand
    ----------------
    This class defines the current command interface for LittleCraft. ♡

    Purpose:
    - Allows players to check or change their "Little Mode" state directly
      via chat commands.
    - Temporarily serves as the only way to toggle regression or adulthood.

    Future plans:
    - Later updates will introduce themed Agere/ABDL items that can
      naturally trigger these transitions (like pacifiers, plushies,
      bottles, or magic age-shift potions).
    - For now, we’re keeping things simple and functional through commands.

    Localization:
    - All feedback messages are now pulled from the mod’s lang files
      using translatable keys for better i18n support.

    Example keys expected in lang:
      littlecraft.command.islittle.true = You are in Little Mode ♡
      littlecraft.command.islittle.false = You are an adult
      littlecraft.command.setlittle.true = You’ve regressed to Little Mode ♡
      littlecraft.command.setlittle.false = You’ve grown up again
*/

public class LittleCommand
{
    public static void register()
    {
        NeoForge.EVENT_BUS.addListener(LittleCommand::onRegister);
    }

    private static void onRegister(RegisterCommandsEvent event)
    {
        // Check current mode
        event.getDispatcher().register(
                Commands.literal("islittle")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            if (LittleData.get(player))
                            {
                                ctx.getSource().sendSuccess(
                                        () -> Component.translatable("littlecraft.command.islittle.true"), false
                                );
                            }
                            else
                            {
                                ctx.getSource().sendSuccess(
                                        () -> Component.translatable("littlecraft.command.islittle.false"), false
                                );
                            }

                            return 1;
                        })
        );

        // Toggle Little Mode manually
        event.getDispatcher().register(
                Commands.literal("little")
                        .then(Commands.literal("true").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            LittleData.set(player, true);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("littlecraft.command.setlittle.true"), false
                            );
                            return 1;
                        }))
                        .then(Commands.literal("false").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            LittleData.set(player, false);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("littlecraft.command.setlittle.false"), false
                            );
                            return 1;
                        }))
        );

        // Set little age — self: /age <number>  other: /age <player> <number>
        event.getDispatcher().register(
                Commands.literal("age")
                        // /age <number>
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
                        // /age <player> <number>
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("age", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int age = IntegerArgumentType.getInteger(ctx, "age");
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
        );
    }
}
