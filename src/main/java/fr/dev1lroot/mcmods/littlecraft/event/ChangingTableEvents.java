/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class ChangingTableEvents
{
    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getTarget() instanceof Player target)) return;

        Player changer = event.getEntity();
        ItemStack held  = changer.getItemInHand(InteractionHand.MAIN_HAND);
        boolean heldDiaper = held.getItem() instanceof Diaper.DiaperItem;
        boolean onTable    = target.getVehicle() instanceof ChangingTableSeatEntity;

        // Only intercept player-on-player diaper-related actions.
        if (!onTable && !heldDiaper) return;

        event.setCanceled(true);

        if (!onTable)
        {
            changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.error.not_on_table"));
            return;
        }

        ItemStack targetLegs   = target.getItemBySlot(EquipmentSlot.LEGS);
        boolean   targetHasDiaper = targetLegs.getItem() instanceof Diaper.DiaperItem;

        if (targetHasDiaper)
        {
            if (!Diaper.isOpen(targetLegs))
            {
                // Closed diaper → open it
                target.setItemSlot(EquipmentSlot.LEGS, Diaper.setOpen(targetLegs, true));
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.opened"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.opened_by_other"));
            }
            else if (!isFresh(targetLegs))
            {
                // Open dirty diaper → remove it, hand back to changer
                target.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                if (!changer.addItem(targetLegs))
                    changer.drop(targetLegs, false);
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.removed"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.removed_by_other"));
            }
            else if (held.isEmpty())
            {
                // Open fresh diaper + empty hand → close it
                target.setItemSlot(EquipmentSlot.LEGS, Diaper.setOpen(targetLegs, false));
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.closed"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.closed_by_other"));
            }
            // open fresh + holding something: no-op (prevent accidents)
        }
        else if (heldDiaper)
        {
            if (isFresh(held))
            {
                // No diaper + fresh diaper in hand → equip open
                ItemStack toEquip = Diaper.setOpen(held.copyWithCount(1), true);
                target.setItemSlot(EquipmentSlot.LEGS, toEquip);
                held.shrink(1);
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.equipped"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.equipped_by_other"));
            }
            else
            {
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.item.diaper.change.error.diaper_used"));
            }
        }
        // else: no diaper on target and nothing useful held — no-op
    }

    private static boolean isFresh(ItemStack stack)
    {
        return Diaper.getUsed(stack) == 0
            && stack.getDamageValue() == 0
            && !Diaper.isPooped(stack);
    }

    private static void playSound(Player near)
    {
        near.level().playSound(null, near.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
