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

package com.storyanvil.cogwheel.client.saui;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.data.StoryCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class SAUI {
    private static final HashMap<ResourceLocation, StoryCodec<AbstractCogComponent>> registry = new HashMap<>();

    /**
     * @apiNote Use {@link com.storyanvil.cogwheel.api.CogwheelAPI#registerCogComponent(ResourceLocation, StoryCodec)}
     */
    @Api.Experimental(since = "2.8.0")
    public static void registerCogComponent(ResourceLocation loc, StoryCodec<AbstractCogComponent> codec) {
        if (loc.getNamespace().equals(CogwheelEngine.MODID))
            throw new RuntimeException("Namespace not allowed!");
        registry.put(loc, codec);
    }

    @Api.Internal @ApiStatus.Internal
    public static void registerInternal() {

    }

    private boolean shouldCloseOnESC = true;
    private boolean renderBackground = true;
    private ArrayList<AbstractCogComponent> components = new ArrayList<>();

    public boolean isShouldCloseOnESC() {
        return shouldCloseOnESC;
    }

    public void setShouldCloseOnESC(boolean shouldCloseOnESC) {
        this.shouldCloseOnESC = shouldCloseOnESC;
    }

    public boolean isRenderBackground() {
        return renderBackground;
    }

    public void setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }

    public ArrayList<AbstractCogComponent> getComponents() {
        return components;
    }

    public void addComponent(AbstractCogComponent cogComponent) {
        components.add(cogComponent);
    }
}
