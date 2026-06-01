/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.entity;

import fr.dev1lroot.mcmods.littlecraft.content.ChangingTable;
import fr.dev1lroot.mcmods.littlecraft.content.block.AbstractChangingTableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ChangingTableSeatEntity extends Entity
{
    private BlockPos tablePos;

    public ChangingTableSeatEntity(EntityType<?> type, Level level)
    {
        super(type, level);
        this.noPhysics = true;
    }

    public ChangingTableSeatEntity(Level level, BlockPos headPos, Direction facing)
    {
        this(ChangingTable.CHANGING_TABLE_SEAT.get(), level);
        this.tablePos = headPos;
        // Centre the entity between the HEAD and FOOT blocks so the lying player
        // spans both.  FACING points from FOOT → HEAD, so HEAD - 0.5*FACING is the midpoint.
        double cx = headPos.getX() + 0.5 - facing.getStepX() * 0.5;
        double cz = headPos.getZ() + 0.5 - facing.getStepZ() * 0.5;
        setPos(cx, headPos.getY() + 0.3, cz);
        // Player should face toward their feet (= opposite of HEAD direction).
        setYRot(facing.getOpposite().toYRot());
    }

    public BlockPos getTablePos() { return tablePos; }

    @Override
    public void tick()
    {
        super.tick();
        if (level().isClientSide()) return;
        if (tablePos == null
                || !(level().getBlockState(tablePos).getBlock() instanceof AbstractChangingTableBlock))
        {
            discard();
            return;
        }
        if (getPassengers().isEmpty())
            discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(ValueInput input)
    {
        tablePos = new BlockPos(
            input.getIntOr("tx", 0),
            input.getIntOr("ty", 0),
            input.getIntOr("tz", 0)
        );
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output)
    {
        if (tablePos != null)
        {
            output.putInt("tx", tablePos.getX());
            output.putInt("ty", tablePos.getY());
            output.putInt("tz", tablePos.getZ());
        }
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amt) { return false; }
    @Override public boolean isPickable()  { return false; }
    @Override public boolean isPushable()  { return false; }
    @Override public boolean isNoGravity() { return true; }

    @Override
    protected boolean canAddPassenger(Entity passenger) { return getPassengers().isEmpty(); }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger)
    {
        if (tablePos == null) return super.getDismountLocationForPassenger(passenger);
        Vec3[] spots = {
            new Vec3(tablePos.getX() + 0.5, tablePos.getY(), tablePos.getZ() - 0.6),
            new Vec3(tablePos.getX() + 0.5, tablePos.getY(), tablePos.getZ() + 1.6),
            new Vec3(tablePos.getX() - 0.6, tablePos.getY(), tablePos.getZ() + 0.5),
            new Vec3(tablePos.getX() + 1.6, tablePos.getY(), tablePos.getZ() + 0.5),
        };
        for (Vec3 spot : spots)
        {
            BlockPos bp = BlockPos.containing(spot);
            if (!level().getBlockState(bp).isSolid() && !level().getBlockState(bp.above()).isSolid())
                return spot;
        }
        return super.getDismountLocationForPassenger(passenger);
    }
}
