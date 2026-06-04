/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlock;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import fr.dev1lroot.mcmods.littlecraft.content.entity.PottySeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class CarryEventHandler
{
    // Tracks carriers currently holding a little so we can detect dismount transitions
    // and push the updated (empty) passenger list to their own client.
    private static final Set<UUID> activeCarriers = new HashSet<>();

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player carrier = event.getEntity();
        if (!(event.getTarget() instanceof Player little)) return;

        if (!carrier.getMainHandItem().isEmpty()) return;

        int carrierAge = LittleData.getAge(carrier);
        int littleAge  = LittleData.getAge(little);

        if (carrierAge < 18) return;
        if (littleAge < 1 || littleAge > 12) return;

        if (little.isPassenger() || carrier.isPassenger() || !carrier.getPassengers().isEmpty()) return;

        little.startRiding(carrier, true, true);

        // The entity tracker skips self-updates (player != this.entity guard in ChunkMap),
        // so the carrier's own client never receives ClientboundSetPassengersPacket via the
        // tracker. Send it directly so their client sets up the riding relationship immediately.
        if (carrier instanceof ServerPlayer serverCarrier) {
            serverCarrier.connection.send(new ClientboundSetPassengersPacket(carrier));
            activeCarriers.add(serverCarrier.getUUID());
        }

        carrier.sendSystemMessage(Component.translatable("littlecraft.carry.started.carrier", little.getName()));
        little.sendSystemMessage(Component.translatable("littlecraft.carry.started.little", carrier.getName()));

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player carrier = event.getEntity();
        Level level    = event.getLevel();
        BlockPos pos   = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (carrier.isShiftKeyDown())
        {
            // Shift + right-click: grab the little one back from the block.
            if (LittleData.getAge(carrier) < 18) return;
            if (!carrier.getMainHandItem().isEmpty()) return;
            if (!carrier.getPassengers().isEmpty()) return;
            if (carrier.isPassenger()) return;

            Player little = findLittleAt(level, state, pos);
            if (little == null) return;

            event.setCanceled(true);
            little.stopRiding();
            if (little.isSleeping()) little.stopSleeping();
            little.startRiding(carrier, true, true);

            if (carrier instanceof ServerPlayer sp)
            {
                sp.connection.send(new ClientboundSetPassengersPacket(carrier));
                activeCarriers.add(sp.getUUID());
            }
            carrier.sendSystemMessage(Component.translatable("littlecraft.carry.started.carrier", little.getName()));
            little.sendSystemMessage(Component.translatable("littlecraft.carry.started.little", carrier.getName()));
        }
        else
        {
            // Right-click: place the carried little one onto the block.
            Player little = carrier.getPassengers().stream()
                .filter(p -> p instanceof Player lp
                    && LittleData.getAge(lp) >= 1 && LittleData.getAge(lp) <= 12)
                .map(p -> (Player) p)
                .findFirst().orElse(null);
            if (little == null) return;

            if (state.getBlock() instanceof AbstractChangingTableBlock changingTable)
            {
                event.setCanceled(true);
                little.stopRiding();
                placeOnChangingTable(level, state, pos, changingTable, little);
            }
            else if (state.getBlock() instanceof PottyBlock)
            {
                event.setCanceled(true);
                little.stopRiding();
                placeOnPotty(level, pos, little);
            }
            else if (state.getBlock() instanceof BedBlock)
            {
                event.setCanceled(true);
                little.stopRiding();
                little.startSleepInBed(pos);
            }
        }
    }

    private static void placeOnChangingTable(Level level, BlockState state, BlockPos pos,
                                             AbstractChangingTableBlock changingTable, Player little)
    {
        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.FOOT
            ? pos.relative(facing) : pos;
        if (!level.getBlockState(headPos).is(changingTable)) return;
        List<ChangingTableSeatEntity> seats = level.getEntities(
            EntityTypeTest.forClass(ChangingTableSeatEntity.class),
            new AABB(headPos).inflate(1.0),
            e -> headPos.equals(e.getTablePos()));
        if (!seats.isEmpty()) return;
        ChangingTableSeatEntity seat = new ChangingTableSeatEntity(level, headPos, facing);
        level.addFreshEntity(seat);
        little.startRiding(seat, true, false);
    }

    private static void placeOnPotty(Level level, BlockPos pos, Player little)
    {
        List<PottySeatEntity> existing = level.getEntities(
            EntityTypeTest.forClass(PottySeatEntity.class),
            new AABB(pos).inflate(0.5),
            e -> pos.equals(e.getPottyPos()));
        if (!existing.isEmpty()) return;
        PottySeatEntity seat = new PottySeatEntity(level, pos);
        level.addFreshEntity(seat);
        little.startRiding(seat, true, false);
    }

    private static Player findLittleAt(Level level, BlockState state, BlockPos pos)
    {
        if (state.getBlock() instanceof AbstractChangingTableBlock)
        {
            Direction facing = state.getValue(BedBlock.FACING);
            BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.FOOT
                ? pos.relative(facing) : pos;
            for (ChangingTableSeatEntity seat : level.getEntities(
                EntityTypeTest.forClass(ChangingTableSeatEntity.class),
                new AABB(headPos).inflate(1.0),
                e -> headPos.equals(e.getTablePos())))
                for (Entity p : seat.getPassengers())
                    if (p instanceof Player lp && LittleData.getAge(lp) >= 1 && LittleData.getAge(lp) <= 12)
                        return lp;
        }
        else if (state.getBlock() instanceof PottyBlock)
        {
            for (PottySeatEntity seat : level.getEntities(
                EntityTypeTest.forClass(PottySeatEntity.class),
                new AABB(pos).inflate(0.5),
                e -> pos.equals(e.getPottyPos())))
                for (Entity p : seat.getPassengers())
                    if (p instanceof Player lp && LittleData.getAge(lp) >= 1 && LittleData.getAge(lp) <= 12)
                        return lp;
        }
        else if (state.getBlock() instanceof BedBlock)
        {
            for (Player p : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(2.0),
                p -> p.isSleeping() && p.getSleepingPos()
                    .map(sp -> sp.equals(pos) || sp.equals(pos.relative(state.getValue(BedBlock.FACING))))
                    .orElse(false)))
                if (LittleData.getAge(p) >= 1 && LittleData.getAge(p) <= 12)
                    return p;
        }
        return null;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer carrier)) return;
        if (carrier.level().isClientSide()) return;

        // Carrier pressing shift ejects the little passenger
        if (carrier.isShiftKeyDown()) {
            for (Entity passenger : List.copyOf(carrier.getPassengers())) {
                if (!(passenger instanceof Player little)) continue;
                int littleAge = LittleData.getAge(little);
                if (littleAge < 1 || littleAge > 12) continue;

                little.stopRiding();
                carrier.sendSystemMessage(Component.translatable("littlecraft.carry.stopped.carrier", little.getName()));
                little.sendSystemMessage(Component.translatable("littlecraft.carry.stopped.little", carrier.getName()));
            }
        }

        // Keep the carrier's own client in sync. The entity tracker never sends the carrier
        // their own entity's passenger changes, so we push the packet on every state transition.
        boolean isCarrying = carrier.getPassengers().stream()
            .anyMatch(p -> p instanceof Player lp
                && LittleData.getAge(lp) >= 1 && LittleData.getAge(lp) <= 12);

        UUID id = carrier.getUUID();
        boolean wasCarrying = activeCarriers.contains(id);

        if (isCarrying) {
            activeCarriers.add(id);
            carrier.connection.send(new ClientboundSetPassengersPacket(carrier));
        } else if (wasCarrying) {
            // Passenger dismounted (little pressed shift or another cause) — clear on carrier's client
            activeCarriers.remove(id);
            carrier.connection.send(new ClientboundSetPassengersPacket(carrier));
        }
    }
}
