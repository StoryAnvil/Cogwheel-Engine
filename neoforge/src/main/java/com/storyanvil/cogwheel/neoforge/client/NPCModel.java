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

package com.storyanvil.cogwheel.neoforge.client;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.entity.NPC;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class NPCModel extends GeoModel<NPC> {

    public static final Identifier ANIMATIONS = Identifier.of(CogwheelEngine.MODID, "animations/entity/npc.animation.json");

    public NPCModel() {
        super();
    }

    @Override
    public Identifier getModelResource(NPC animatable, GeoRenderer<NPC> renderer) {
        return animatable.platformModel;
    }

    @Override
    public Identifier getTextureResource(NPC animatable, GeoRenderer<NPC> renderer) {
        return animatable.platformTexture;
    }

    @Override
    public Identifier getAnimationResource(NPC animatable) {
        return ANIMATIONS;
    }

    @Override
    public Identifier[] getAnimationResourceFallbacks(NPC animatable, GeoRenderer<NPC> renderer) {
        return animationSources;
    }

    @ApiStatus.Internal
    public static Identifier[] animationSources = new Identifier[0];
}
