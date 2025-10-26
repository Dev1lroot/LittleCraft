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
    private static final ResourceLocation DIAPER_TEXTURE =
            ResourceLocation.parse("littlecraft:textures/texture.png");
    private static DiaperModel<Player> MODEL; // изначально null

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();

        // Надето ли наше «LEGS»
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!(legs.getItem() instanceof Diaper.DiaperItem)) return;

        if (MODEL == null) {
            MODEL = new DiaperModel<>(Minecraft.getInstance()
                    .getEntityModels().bakeLayer(DiaperModel.LAYER_LOCATION));
        }

        Item diaper = legs.getItem();

        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        // 1) ДОБАВЛЯЕМ ГЛОБАЛЬНЫЙ ПОВОРОТ ТЕЛА (yaw)
        float pt = event.getPartialTick();
        float bodyYaw = net.minecraft.util.Mth.lerp(pt, player.yBodyRotO, player.yBodyRot);
        // В Post глобальный поворот уже «снят», поэтому вернём его.
        // Знак может отличаться в зависимости от твоей модели — если будет развёрнуто, поменяй на -bodyYaw.
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-bodyYaw));

        // 2) ПРИВЯЗКА К КОСТИ ТЕЛА
        var playerModel = event.getRenderer().getModel();
        playerModel.body.translateAndRotate(pose);

        // 3) КОРРЕКТНАЯ АНИМАЦИЯ (шаги и т.п.)
        // В 1.20+:
        float limbSwing = player.walkAnimation.position(pt);
        float limbSwingAmount = player.walkAnimation.speed();
        float ageInTicks = player.tickCount + pt;
        float headYaw = net.minecraft.util.Mth.lerp(pt, player.yHeadRotO, player.getYHeadRot());
        float headPitch = net.minecraft.util.Mth.lerp(pt, player.xRotO, player.getXRot());

        // Если твой DiaperModel рендерится поверх корпуса, чаще всего анимации ног не нужны,
        // но пусть будут корректные значения.
        MODEL.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);

        // (необязательно, но полезно) — если у DiaperModel есть часть "body", можно скопировать локальную позу:
        // MODEL.body.copyFrom(playerModel.body);

        MODEL.renderToBuffer(
                pose,
                event.getMultiBufferSource().getBuffer(RenderType.entityCutout(DIAPER_TEXTURE)),
                event.getPackedLight(),
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                0xFFFFFF
        );

        pose.popPose();
    }

}
