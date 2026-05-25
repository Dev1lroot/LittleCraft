package fr.dev1lroot.mcmods.littlecraft.mixin.client;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class LittleHeadScaleMixin
{
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void littlecraft$applyLittleHeadScale(AvatarRenderState state, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof Player player)) return;

        if (LittleData.get(player))
        {
            PlayerModel model = (PlayerModel) (Object) this;
            
            // Correct head side, we scale the player 2 times smaller but keeping their head intact so making it 2 times bigger;
            model.head.xScale = 2.0F;
            model.head.yScale = 2.0F;
            model.head.zScale = 2.0F;
        }
    }
}
