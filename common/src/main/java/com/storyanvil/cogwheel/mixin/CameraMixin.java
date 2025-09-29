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

package com.storyanvil.cogwheel.mixin;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.client.CutSceneManagement;
import com.storyanvil.cogwheel.data.CameraPos;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Shadow protected abstract void setPos(Vec3d pos);

    @Shadow protected abstract void setPos(double x, double y, double z);

    @Shadow private boolean thirdPerson;

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract float getYaw();

    @Shadow public abstract float getPitch();

    @Inject(method = "update", at = @At("RETURN"))
    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        try {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            boolean detach = false;
            CameraPos force = CutSceneManagement.getPositionForce();
            if (force != null) {
                this.setPos(force.getPos());
                setRotation(force.getRotY(), force.getRotX());
                //noinspection UnusedAssignment
                detach = true;
                return;
            }

            Bi<CameraPos, CameraPos> transition = CutSceneManagement.getTransition();
            if (transition != null) {
                boolean upd = false;
                if (transition.getA() == null) {
                    transition.setA(new CameraPos(getPos(), getYaw(), getPitch()));
                    upd = true;
                }
                if (transition.getB() == null) {
                    transition.setB(new CameraPos(getPos(), getYaw(), getPitch()));
                    upd = true;
                }
                if (upd) {
                    CutSceneManagement.setTransition(transition);
                }
                float delta = CutSceneManagement.getTransitionDelta() + tickDelta;
                CutSceneManagement.setTransitionDelta(delta);
                float progress = MathHelper.clamp((delta + 0.01f) / CutSceneManagement.getTransitionGoal(), 0f, 1f);
                Vec3d A = transition.getA().getPos();
                Vec3d B = transition.getB().getPos();
                setPos(MathHelper.lerp(progress, A.x, B.x), MathHelper.lerp(progress, A.y, B.y), MathHelper.lerp(progress, A.z, B.z));
                setRotation(MathHelper.lerpAngleDegrees(progress, transition.getA().getRotY(), transition.getB().getRotY()),
                        MathHelper.lerpAngleDegrees(progress, transition.getA().getRotX(), transition.getB().getRotX()));
                detach = true;
            }

            this.thirdPerson |= detach;
            CutSceneManagement.setRenderHand(!detach);
        } catch (Throwable t) {
            CogwheelEngine.LOGGER.error("Camera error", t);
        }
    }
}
