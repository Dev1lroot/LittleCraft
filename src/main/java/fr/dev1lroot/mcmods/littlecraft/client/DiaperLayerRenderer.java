package fr.dev1lroot.mcmods.littlecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.DiaperModel;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class DiaperLayerRenderer
{
    // The diaper fashion statement texture. 100% absorbency guaranteed.
    private static final ResourceLocation DIAPER_TEXTURE =
            ResourceLocation.parse("littlecraft:textures/texture.png");

    // Lazy diaper model – it only spawns when needed, like a baby waking up at 3 AM.
    private static DiaperModel<Player> MODEL;

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event)
    {
        Player player = event.getEntity();

        // Time to check the booty slot. Is baby padded or nah?
        ItemStack butt = player.getItemBySlot(EquipmentSlot.LEGS);

        // No diaper? No render. No bab vibes allowed.
        if (!(butt.getItem() instanceof Diaper.DiaperItem)) return;

        // Summon the sacred diaper model if it doesn't exist yet.
        if (MODEL == null) {
            MODEL = new DiaperModel<>(Minecraft.getInstance()
                    .getEntityModels().bakeLayer(DiaperModel.LAYER_LOCATION));
        }

        // Grab the actual diaper item. Handle with care, it's crinkly.
        Item diaper = butt.getItem();

        PoseStack pose = event.getPoseStack();
        pose.pushPose(); // entering padded dimension

        // Sync up with the player's torso, so the diaper hugs snugly.
        var playerModel = event.getRenderer().getModel();
        playerModel.body.translateAndRotate(pose);

        // Make sure the diaper doesn't free-spin like a rogue pacifier.
        float pt = event.getPartialTick();
        float bodyYaw = net.minecraft.util.Mth.lerp(pt, player.yBodyRotO, player.yBodyRot);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-bodyYaw));

        // Harvest walky-walky and waddle animations from the player.
        float limbSwing = player.walkAnimation.position(pt);
        float limbSwingAmount = player.walkAnimation.speed();
        float ageInTicks = player.tickCount + pt;
        float headYaw = net.minecraft.util.Mth.lerp(pt, player.yHeadRotO, player.getYHeadRot());
        float headPitch = net.minecraft.util.Mth.lerp(pt, player.xRotO, player.getXRot());

        // Teach the diaper how to wiggle with maximum crinkle physics.
        MODEL.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);

        // Report diaper condition: fresh, soggy, or absolutely destroyed.
        MODEL.damage = diaper.getDamage(butt);

        // Behold: the ultimate padded protection layer, rendered in glorious HD crinkles.
        MODEL.renderToBuffer(
                pose,
                event.getMultiBufferSource().getBuffer(RenderType.entityCutout(DIAPER_TEXTURE)),
                event.getPackedLight(),
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                0xFFFFFF
        );

        pose.popPose(); // leaving padded dimension, back to big-kid land
    }
}
