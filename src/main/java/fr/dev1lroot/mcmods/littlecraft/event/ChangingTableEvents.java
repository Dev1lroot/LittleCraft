/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class ChangingTableEvents
{
    // Block right-click: changer clicks the changing table surface directly.
    // This fires before useWithoutItem and is the primary diaper-change path
    // when clicking the player's hitbox is impractical.
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.isCanceled()) return;
        if (event.getEntity().isShiftKeyDown()) return; // shift-click = grab-back (CarryEventHandler)
        if (!event.getEntity().getPassengers().isEmpty()) return; // carrying someone (CarryEventHandler)

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractChangingTableBlock)) return;

        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.FOOT
            ? pos.relative(facing) : pos;

        for (ChangingTableSeatEntity seat : level.getEntities(
            EntityTypeTest.forClass(ChangingTableSeatEntity.class),
            new AABB(headPos).inflate(1.0),
            e -> headPos.equals(e.getTablePos())))
        {
            for (Entity passenger : seat.getPassengers())
            {
                if (!(passenger instanceof Player target)) continue;
                event.setCanceled(true);
                performDiaperChange(event.getEntity(), target);
                return;
            }
        }
        // No player on table — let useWithoutItem handle mounting normally.
    }

    // Entity right-click: changer clicks the little player directly (hitbox path).
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

        if (!onTable && !heldDiaper) return;

        event.setCanceled(true);

        if (!onTable)
        {
            changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.error.not_on_table"));
            return;
        }

        performDiaperChange(changer, target);
    }

    public static void performDiaperChange(Player changer, Player target)
    {
        ItemStack held        = changer.getItemInHand(InteractionHand.MAIN_HAND);
        boolean   heldDiaper  = held.getItem() instanceof Diaper.DiaperItem;
        ItemStack targetLegs  = target.getItemBySlot(EquipmentSlot.LEGS);
        boolean   hasDiaper   = targetLegs.getItem() instanceof Diaper.DiaperItem;

        if (hasDiaper)
        {
            if (!Diaper.isOpen(targetLegs))
            {
                target.setItemSlot(EquipmentSlot.LEGS, Diaper.setOpen(targetLegs, true));
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.opened"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.opened_by_other"));
            }
            else if (!isFresh(targetLegs))
            {
                target.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                if (!changer.addItem(targetLegs)) changer.drop(targetLegs, false);
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.removed"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.removed_by_other"));
            }
            else if (held.isEmpty())
            {
                target.setItemSlot(EquipmentSlot.LEGS, Diaper.setOpen(targetLegs, false));
                playSound(changer);
                changer.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.closed"));
                target.sendSystemMessage(Component.translatable("littlecraft.notification.diaper.change.closed_by_other"));
            }
            // open fresh + holding something: no-op
        }
        else if (heldDiaper)
        {
            if (isFresh(held))
            {
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
        // else: no diaper on target, nothing useful held — no-op
    }

    private static boolean isFresh(ItemStack stack)
    {
        return Diaper.isPrepared(stack)
            && Diaper.getUsed(stack) == 0
            && !Diaper.isPooped(stack);
    }

    private static void playSound(Player near)
    {
        near.level().playSound(null, near.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
