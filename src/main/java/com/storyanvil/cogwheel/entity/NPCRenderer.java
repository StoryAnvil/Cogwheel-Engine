package com.storyanvil.cogwheel.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NPCRenderer extends MobRenderer<NPC, NPCModel<NPC>> {
    public static final ModelLayerLocation NPC_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, "npc_layer"),
            "main");

    public NPCRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new NPCModel<>(pContext.bakeLayer(NPC_LAYER)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull NPC pEntity) {
        return ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, "textures/entity/npc/" + pEntity.getSkin() + ".png");
    }

    @Override
    public void render(NPC pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}