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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.client.CutSceneManagement;
import com.storyanvil.cogwheel.client.screen.DialogChoiceScreen;
import com.storyanvil.cogwheel.client.screen.DialogMessageScreen;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.infrastructure.abilities.StoryAnimator;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogwheelClientPacketHandler {
    public static void animationBound(AnimationBound animationBound, IStoryPacketContext ctx) {
        CogwheelExecutor.scheduleTickEventClientSide(level -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                log.warn("Failed to run animation bound as LocalPlayer is NULL!");
                return;
            }
            BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
            int d = 1000;
            Box aabb = new Box(pos.south(d).east(d).down(d).toCenterPos(), pos.north(d).west(d).up(d).toCenterPos());
//            level.getEntities((Entity) null, aabb, entity -> (entity instanceof StoryAnimator animator && animator.getAnimatorID().equals(animationBound.getAnimatorID())));

            List<Entity> e = level.getEntitiesByType(new TypeFilter<Entity, Entity>() {
                @Override
                public @Nullable Entity downcast(Entity obj) {
                    if (obj instanceof StoryAnimator animator) return obj;
                    return null;
                }

                @Override
                public Class<? extends Entity> getBaseClass() {
                    return Entity.class;
                }
            }, aabb, entity -> {
                return true;
            });
            if (e.isEmpty()) {
                log.warn("Failed to run animation bound for {}->{} | No StoryAnimators found", animationBound.animatorID(), animationBound.animation());
                return;
            }
            for (Entity entity : e) {
                StoryAnimator animator = (StoryAnimator) entity;
                animator.pushAnimation(animationBound.animation());
            }
        });
    }
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
