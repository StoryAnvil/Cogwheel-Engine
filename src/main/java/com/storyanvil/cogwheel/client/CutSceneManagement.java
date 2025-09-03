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

package com.storyanvil.cogwheel.client;

import com.storyanvil.cogwheel.data.CameraPos;
import com.storyanvil.cogwheel.util.Bi;

public class CutSceneManagement {
    private static CameraPos positionForce = null;
    private static Bi<CameraPos, CameraPos> transition = null;
    private static float transitionDelta = 0f;
    private static float transitionGoal = 0f;
    private static boolean renderHand = true;

    public static synchronized CameraPos getPositionForce() {
        return positionForce;
    }

    public static synchronized void setPositionForce(CameraPos positionForce) {
        CutSceneManagement.positionForce = positionForce;
    }

    public static synchronized Bi<CameraPos, CameraPos> getTransition() {
        return transition;
    }

    public static synchronized void setTransition(Bi<CameraPos, CameraPos> transition) {
        if (transition.getA() == null && transition.getB() == null) {
            CutSceneManagement.transition = null;
            CutSceneManagement.transitionDelta = 0f;
            return;
        }
        CutSceneManagement.transition = transition;
        CutSceneManagement.transitionDelta = 0f;
    }

    public static float getTransitionDelta() {
        return transitionDelta;
    }

    public static void setTransitionDelta(float transitionDelta) {
        CutSceneManagement.transitionDelta = transitionDelta;
    }

    public static synchronized float getTransitionGoal() {
        return transitionGoal;
    }

    public static synchronized void setTransitionGoal(float transitionGoal) {
        CutSceneManagement.transitionGoal = transitionGoal;
    }

    public static boolean isRenderHand() {
        return renderHand;
    }

    public static void setRenderHand(boolean renderHand) {
        CutSceneManagement.renderHand = renderHand;
    }
}
