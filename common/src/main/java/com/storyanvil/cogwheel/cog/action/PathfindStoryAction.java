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

package com.storyanvil.cogwheel.cog.action;

import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PathfindStoryAction extends StoryAction<AbstractNPC<?>> {
    private final BlockPos target;

    public PathfindStoryAction(BlockPos target) {
        super();
        this.target = target;
    }

    @Override
    public void proceed(@NotNull AbstractNPC<?> myself) {
        EntityNavigation navigation = myself.npc$getNavigation();
        Path path = navigation.findPathTo(target.getX(), target.getY(), target.getZ(), 0);
        navigation.startMovingAlong(path, 2);
    }

    @Override
    public boolean freeToGo(@NotNull AbstractNPC<?> myself) {
        boolean done = !myself.npc$getNavigation().isIdle();
        if (done) onExit();
        return done;
    }
}
