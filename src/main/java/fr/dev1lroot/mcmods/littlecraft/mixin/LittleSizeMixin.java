package fr.dev1lroot.mcmods.littlecraft.mixin;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
    LittleSizeMixin
    ----------------
    This server-side mixin exists purely to handle hitbox scaling
    for players in “Little Mode”. ♡

    It doesn’t change rendering or visuals — only the physical
    size that the world and blocks see, so littles can:
      - Walk through 1-block spaces (when standing)
      - Crawl under trapdoors (when sneaking)

    Without this, the client might *look* tiny, but the server
    would still treat the player like a full-sized grown-up.
    This mixin makes sure their hitbox truly matches their
    little size and behavior! 🧸
*/

@Mixin(LivingEntity.class)
public abstract class LittleSizeMixin
{
    @Inject(
            method = "getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void littlecraft$modifyDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir)
    {
        LivingEntity entity = (LivingEntity)(Object)this;

        // Only apply to players — mobs don’t use Little Mode ♡
        if (entity instanceof Player player)
        {
            if (LittleData.get(player))
            {
                float width = 0.35F;
                float height = 0.9F;

                // If the little one is crouching, make them even tinier
                if (player.isCrouching())
                {
                    height = 0.7F;
                }

                // Apply new server-side hitbox dimensions
                cir.setReturnValue(EntityDimensions.scalable(width, height));
            }
        }
    }
}