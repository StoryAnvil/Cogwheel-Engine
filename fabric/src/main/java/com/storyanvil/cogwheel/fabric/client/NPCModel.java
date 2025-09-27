/*
 *
 * StoryAnvil Cogwheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.fabric.client;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.entity.NPC;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class NPCModel extends DefaultedEntityGeoModel<NPC> {
    public NPCModel(Identifier assetSubpath) {
        super(assetSubpath);
    }

    /*@Override
    public Identifier getTextureResource(@NotNull NPC animatable) {
        return Identifier.of(CogwheelEngine.MODID, "textures/entity/npc/" + animatable.getSkin() + ".png");
    }

    @Override
    public Identifier getTextureResource(NPC animatable, @Nullable GeoRenderer<NPC> renderer) {
        return this.getTextureResource(animatable);
    }

    @Override
    public Identifier[] getAnimationResourceFallbacks(NPC animatable) {
        return animationSources;
    }

    @Override
    public Identifier getModelResource(NPC animatable) {
        return Identifier.of(CogwheelEngine.MODID, "geo/entity/" + animatable.getStoryModelID() + ".geo.json");
    }

    @Override
    public Identifier getModelResource(NPC animatable, @Nullable GeoRenderer<NPC> renderer) {
        return this.getModelResource(animatable);
    }
*/
    @ApiStatus.Internal
    public static Identifier[] animationSources = new Identifier[0];
}
