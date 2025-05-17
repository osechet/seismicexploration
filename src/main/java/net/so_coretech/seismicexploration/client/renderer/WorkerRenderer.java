package net.so_coretech.seismicexploration.client.renderer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.entity.WorkerEntity;

public class WorkerRenderer extends HumanoidMobRenderer<WorkerEntity, PlayerRenderState, PlayerModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "textures/entity/worker2.png");

    public WorkerRenderer(final EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public PlayerRenderState createRenderState() {
        return new PlayerRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(final PlayerRenderState pRenderState) {
        return TEXTURE;
    }
}
