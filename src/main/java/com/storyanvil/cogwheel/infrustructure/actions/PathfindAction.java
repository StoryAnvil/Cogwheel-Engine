/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.infrustructure.actions;

import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

public class PathfindAction extends StoryAction<StoryNavigator> {
    private final BlockPos target;

    public PathfindAction(BlockPos target) {
        super();
        this.target = target;
    }

    @Override
    public void proceed(@NotNull StoryNavigator myself) {
        PathNavigation navigation = myself.getNavigation();
        Path path = navigation.createPath(target.getX(), target.getY(), target.getZ(), 0);
        myself.getNavigation().moveTo(path, 2);
    }

    @Override
    public boolean freeToGo(@NotNull StoryNavigator myself) {
        boolean done = myself.getNavigation().isDone();
        if (done) hitLabel();
        return done;
    }
}
