package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
            renderLittle(event);
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

    private static void renderLittle(RenderPlayerEvent.Pre<?> event)
    {
        PoseStack poseStack = event.getPoseStack();
        PlayerModel model = event.getRenderer().getModel();

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        model.head.xScale = 2.0F;
        model.head.yScale = 2.0F;
        model.head.zScale = 2.0F;
    }

    private static void renderAdult(RenderPlayerEvent.Pre<?> event)
    {
        PoseStack poseStack = event.getPoseStack();
        PlayerModel model = event.getRenderer().getModel();

        poseStack.pushPose();

        poseStack.scale(1F, 1F, 1F);
        model.head.xScale = 1.0F;
        model.head.yScale = 1.0F;
        model.head.zScale = 1.0F;
    }
}
