package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class LittlePlayerRenderer
{
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event)
    {
        AvatarRenderState renderState = event.getRenderState();

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(renderState.id);
        if (!(entity instanceof Player player)) return;

        if (LittleData.get(player))
        {
            renderLittle(event, renderState);
        }
        else
        {
            renderAdult(event);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event)
    {
        event.getPoseStack().popPose();
    }

    // head top in world-space = (1.5 + 24/16) * 0.5 = 1.5 blocks; place tag just above that
    private static final float LITTLE_NAMETAG_Y = 2.8F;

    private static void renderLittle(RenderPlayerEvent.Pre<?> event, AvatarRenderState state)
    {
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);

        if (state.nameTagAttachment != null)
        {
            state.nameTagAttachment = new Vec3(state.nameTagAttachment.x, LITTLE_NAMETAG_Y, state.nameTagAttachment.z);
        }
    }

    private static void renderAdult(RenderPlayerEvent.Pre<?> event)
    {
        event.getPoseStack().pushPose();
    }
}
