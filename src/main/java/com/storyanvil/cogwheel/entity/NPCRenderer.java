/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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