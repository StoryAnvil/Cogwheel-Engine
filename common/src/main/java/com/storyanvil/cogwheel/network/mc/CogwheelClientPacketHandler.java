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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.client.CutSceneManagement;
import com.storyanvil.cogwheel.client.screen.DialogChoiceScreen;
import com.storyanvil.cogwheel.client.screen.DialogMessageScreen;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.MinecraftClient;

public class CogwheelClientPacketHandler {
    public static Object dialogChoiceBound(DialogChoiceBound bound, IStoryPacketContext ctx) {
        if (bound.close()) {
            MinecraftClient.getInstance().setScreen(null);
        } else {
            MinecraftClient.getInstance().setScreen(new DialogChoiceScreen(bound));
        }
        return null;
    }
    public static void dialogBound(DialogBound bound, IStoryPacketContext ctx) {
        if (bound.close()) {
            MinecraftClient.getInstance().setScreen(null);
        } else {
            MinecraftClient.getInstance().setScreen(new DialogMessageScreen(bound));
        }
    }

    public static void cameraForce(CameraForceBound cameraForceBound, IStoryPacketContext ctx) {
        CutSceneManagement.setPositionForce(cameraForceBound.pos());
    }

    public static void cameraTrans(CameraTransitionBound cameraTransitionBound, IStoryPacketContext ctx) {
        CutSceneManagement.setTransitionGoal(cameraTransitionBound.goal());
        CutSceneManagement.setTransition(new Bi<>(cameraTransitionBound.pos1(), cameraTransitionBound.pos2()));
    }
}
