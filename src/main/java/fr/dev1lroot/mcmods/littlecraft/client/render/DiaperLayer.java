package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import fr.dev1lroot.mcmods.littlecraft.model.DiaperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.ItemStack;

public class DiaperLayer<S extends HumanoidRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M>
{
    private final DiaperModel primaryModel;
    private final DiaperModel wetnessModel;

    public DiaperLayer(RenderLayerParent<S, M> parent, EntityModelSet modelSet)
    {
        super(parent);
        this.primaryModel = new DiaperModel(modelSet.bakeLayer(DiaperModel.LAYER_LOCATION));
        this.primaryModel.overlay.visible = false;

        this.wetnessModel = new DiaperModel(modelSet.bakeLayer(DiaperModel.LAYER_LOCATION));
        this.wetnessModel.primary.visible = false;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot)
    {
        // state.legsEquipment is populated now because the diaper's Equippable has an assetId,
        // which satisfies HumanoidMobRenderer.getEquipmentIfRenderable()'s shouldRender() check.
        ItemStack legs = state.legsEquipment;
        if (legs.isEmpty()) return;
        if (!(legs.getItem() instanceof Diaper.DiaperItem)) return;

        int damage = legs.getDamageValue();
        primaryModel.damage = damage;
        wetnessModel.damage = damage;

        primaryModel.setupAnim(state);
        wetnessModel.setupAnim(state);

        renderColoredCutoutModel(primaryModel, DiaperModel.TEXTURE_PRIMARY, pose, collector, light, state, -1, -1);

        if (damage > 0)
        {
            int alpha = Math.min(damage, 255);
            int color = (alpha << 24) | 0x00FFFFFF;
            collector.order(color)
                    .submitModel(
                            wetnessModel,
                            state,
                            pose,
                            RenderTypes.armorTranslucent(DiaperModel.TEXTURE_WETNESS),
                            light,
                            LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                            color,
                            null,
                            state.outlineColor,
                            null
                    );
        }
    }
}
