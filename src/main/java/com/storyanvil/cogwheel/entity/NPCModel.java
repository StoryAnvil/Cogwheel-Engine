/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.entity;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class NPCModel extends DefaultedEntityGeoModel<NPC> {
    public NPCModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public ResourceLocation getTextureResource(@NotNull NPC animatable) {
        return ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, "textures/entity/npc/" + animatable.getSkin() + ".png");
    }

    @Override
    public ResourceLocation getTextureResource(NPC animatable, @Nullable GeoRenderer<NPC> renderer) {
        return this.getTextureResource(animatable);
    }

    @Override
    public ResourceLocation[] getAnimationResourceFallbacks(NPC animatable) {
        return animationSources;
    }

    @ApiStatus.Internal
    public static ResourceLocation[] animationSources = new ResourceLocation[0];
}
