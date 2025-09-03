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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.client.CutSceneManagement;
import com.storyanvil.cogwheel.client.screen.DialogChoiceScreen;
import com.storyanvil.cogwheel.client.screen.DialogMessageScreen;
import com.storyanvil.cogwheel.infrastructure.abilities.StoryAnimator;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogwheelClientPacketHandler {
    public static void animationBound(AnimationBound animationBound, Supplier<NetworkEvent.Context> ctx) {
        CogwheelExecutor.scheduleTickEventClientSide(levelTickEvent -> {
            ClientLevel level = (ClientLevel) levelTickEvent.level;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                log.warn("Failed to run animation bound as LocalPlayer is NULL!");
                return;
            }
            BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
            int d = 1000;
            AABB aabb = new AABB(pos.south(d).east(d).below(d), pos.north(d).west(d).above(d));
            List<Entity> e = level.getEntities((Entity) null, aabb, entity -> (entity instanceof StoryAnimator animator && animator.getAnimatorID().equals(animationBound.getAnimatorID())));
            if (e.isEmpty()) {
                log.warn("Failed to run animation bound for {}->{} | No StoryAnimators found", animationBound.getAnimatorID(), animationBound.getAnimation());
                return;
            }
            for (Entity entity : e) {
                StoryAnimator animator = (StoryAnimator) entity;
                animator.pushAnimation(animationBound.getAnimation().replace('_','.'));
            }
        });
    }
    public static Object dialogChoiceBound(DialogChoiceBound bound, Supplier<NetworkEvent.Context> contextSupplier) {
        if (bound.isClose()) {
            Minecraft.getInstance().setScreen(null);
        } else {
            Minecraft.getInstance().setScreen(new DialogChoiceScreen(bound));
        }
        return null;
    }
    public static Object dialogBound(DialogBound bound, Supplier<NetworkEvent.Context> contextSupplier) {
        if (bound.isClose()) {
            Minecraft.getInstance().setScreen(null);
        } else {
            Minecraft.getInstance().setScreen(new DialogMessageScreen(bound));
        }
        return null;
    }

    public static Object cameraForce(CameraForceBound cameraForceBound, Supplier<NetworkEvent.Context> contextSupplier) {
        CutSceneManagement.setPositionForce(cameraForceBound.getPos());
        return null;
    }

    public static Object cameraTrans(CameraTransitionBound cameraTransitionBound, Supplier<NetworkEvent.Context> contextSupplier) {
        CutSceneManagement.setTransitionGoal(cameraTransitionBound.getGoal());
        CutSceneManagement.setTransition(new Bi<>(cameraTransitionBound.getPos1(), cameraTransitionBound.getPos2()));
        return null;
    }
}
