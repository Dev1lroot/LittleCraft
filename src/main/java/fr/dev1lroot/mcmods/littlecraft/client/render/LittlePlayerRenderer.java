package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/*
    LittlePlayerRenderer
    ---------------------
    Client-side renderer responsible for visually adjusting players
    based on their LittleCraft state (Little or Adult). ♡

    Function:
    - When a player is in “Little Mode”, their entire model is scaled
      down to 0.5x using PoseStack.
    - The head is then scaled back up slightly to keep proportions
      cute and readable.
    - Adult players render normally at full scale (1.0x).

    Known Issue (unfixed):
    - Because we apply scaling directly via PoseStack, the player’s
      nameplate (nickname tag) becomes misaligned and deformed.
      This happens because the label inherits the same transformation
      matrix as the model. Currently, this is a known rendering bug
      that still needs a dedicated fix. 🧸

    Notes:
    - This class is CLIENT-SIDE ONLY (annotated with @OnlyIn(Dist.CLIENT)).
    - It does not affect hitboxes, physics, or interactions — only visuals.
*/

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class LittlePlayerRenderer
{
    // Handle scaling right before rendering starts
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event)
    {
        Player player = event.getEntity();

        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;

        if (LittleData.get(player))
        {
            renderLittle(event);
        }
        else
        {
            renderAdult(event);
        }
    }

    // Restore PoseStack state after rendering
    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event)
    {
        event.getPoseStack().popPose();
    }

    // Apply scaling for Little players
    private static void renderLittle(RenderPlayerEvent.Pre event)
    {
        PoseStack poseStack = event.getPoseStack();
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        poseStack.pushPose();

        // Make the player look tiny and soft ♡
        poseStack.scale(0.5F, 0.5F, 0.5F);

        // Adjust head scaling to keep proportions readable
        model.head.xScale = 2.0F;
        model.head.yScale = 2.0F;
        model.head.zScale = 2.0F;
    }

    // Reset scaling for adult players
    private static void renderAdult(RenderPlayerEvent.Pre event)
    {
        PoseStack poseStack = event.getPoseStack();
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        poseStack.pushPose();

        // Full-size, normal scale
        poseStack.scale(1F, 1F, 1F);
        model.head.xScale = 1.0F;
        model.head.yScale = 1.0F;
        model.head.zScale = 1.0F;
    }
}
