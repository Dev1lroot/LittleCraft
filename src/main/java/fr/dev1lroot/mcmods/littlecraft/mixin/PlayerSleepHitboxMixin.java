/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Replace the bounding box for sleeping little players with a bed-spanning AABB.
// EntityDimensions only supports a square XZ footprint, so we can't get a 2×1 box
// through getDimensions(). Instead we intercept makeBoundingBox(Vec3) and build
// the AABB directly: 2 blocks along the bed axis, 1 block across, 0.3 tall.
// Centering on the entity position (which is always one block-center of the bed)
// ensures both the head and foot blocks are covered.
@Mixin(Entity.class)
public abstract class PlayerSleepHitboxMixin
{
    @Inject(
        method = "makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void littlecraft$bedHitbox(Vec3 pos, CallbackInfoReturnable<AABB> cir)
    {
        Entity self = (Entity)(Object)this;
        if (!(self instanceof Player player)) return;

        // Guard first: skip when neither state is active to avoid touching attachments
        // during Entity.<init> where player.connection is still null.
        boolean onTable  = player.getVehicle() instanceof ChangingTableSeatEntity;
        boolean sleeping = player.isSleeping();
        if (!onTable && !sleeping) return;

        if (!LittleData.isLittle(player)) return;
        if (LittleData.getAgeBlend(LittleData.getAge(player)) >= 1.0F) return;

        // Changing table: rider of a ChangingTableSeatEntity.
        // The seat is already at the midpoint, so pos is centred — no extra shift needed.
        if (onTable && player.getVehicle() instanceof ChangingTableSeatEntity seat)
        {
            BlockPos tablePos = seat.getTablePos();
            if (tablePos == null) return;
            BlockState tableState = player.level().getBlockState(tablePos);
            if (!(tableState.getBlock() instanceof AbstractChangingTableBlock)) return;
            Direction tableDir = tableState.getValue(BedBlock.FACING);
            double halfX = tableDir.getStepX() != 0 ? 1.0 : 0.5;
            double halfZ = tableDir.getStepZ() != 0 ? 1.0 : 0.5;
            cir.setReturnValue(new AABB(
                pos.x - halfX, pos.y,       pos.z - halfZ,
                pos.x + halfX, pos.y + 0.3, pos.z + halfZ
            ));
            return;
        }

        // Bed / crib: sleeping player.
        if (!sleeping) return;
        Direction bedDir = player.getBedOrientation();
        if (bedDir == null) return;

        // Entity is at the head block center; shift the AABB center 0.5 blocks toward
        // the foot so the box spans both bed blocks symmetrically.
        double cx = pos.x - bedDir.getStepX() * 0.5;
        double cz = pos.z - bedDir.getStepZ() * 0.5;

        // 2 blocks along bed axis, 1 block perpendicular, 0.3 tall
        double halfAlong = 1.0;
        double halfPerp  = 0.5;
        double halfX = bedDir.getStepX() != 0 ? halfAlong : halfPerp;
        double halfZ = bedDir.getStepZ() != 0 ? halfAlong : halfPerp;

        cir.setReturnValue(new AABB(
            cx - halfX, pos.y,       cz - halfZ,
            cx + halfX, pos.y + 0.3, cz + halfZ
        ));
    }
}
