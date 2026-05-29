/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.entity;

import fr.dev1lroot.mcmods.littlecraft.content.Potty;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlock;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlockEntity;
import net.minecraft.core.BlockPos;
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

public class PottySeatEntity extends Entity
{
    private BlockPos pottyPos;

    public PottySeatEntity(EntityType<?> type, Level level)
    {
        super(type, level);
        this.noPhysics = true;
    }

    public PottySeatEntity(Level level, BlockPos pottyPos)
    {
        this(Potty.POTTY_SEAT.get(), level);
        this.pottyPos = pottyPos;
        setPos(pottyPos.getX() + 0.5, pottyPos.getY() - 0.5, pottyPos.getZ() + 0.5);
    }

    public BlockPos getPottyPos()
    {
        return pottyPos;
    }

    public PottyBlockEntity getPottyBlockEntity()
    {
        if (level().isClientSide() || pottyPos == null) return null;
        if (level().getBlockEntity(pottyPos) instanceof PottyBlockEntity be) return be;
        return null;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!level().isClientSide())
        {
            if (pottyPos == null || !(level().getBlockState(pottyPos).getBlock() instanceof PottyBlock))
            {
                discard();
                return;
            }
            if (getPassengers().isEmpty())
                discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(ValueInput input)
    {
        int x = input.getIntOr("px", 0);
        int y = input.getIntOr("py", 0);
        int z = input.getIntOr("pz", 0);
        pottyPos = new BlockPos(x, y, z);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output)
    {
        if (pottyPos != null)
        {
            output.putInt("px", pottyPos.getX());
            output.putInt("py", pottyPos.getY());
            output.putInt("pz", pottyPos.getZ());
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return getPassengers().isEmpty();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger)
    {
        if (pottyPos == null) return super.getDismountLocationForPassenger(passenger);
        Vec3[] spots = {
            new Vec3(pottyPos.getX() + 0.5, pottyPos.getY(), pottyPos.getZ() - 0.6),
            new Vec3(pottyPos.getX() + 0.5, pottyPos.getY(), pottyPos.getZ() + 1.6),
            new Vec3(pottyPos.getX() - 0.6, pottyPos.getY(), pottyPos.getZ() + 0.5),
            new Vec3(pottyPos.getX() + 1.6, pottyPos.getY(), pottyPos.getZ() + 0.5),
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
