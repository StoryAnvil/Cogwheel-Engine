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

package com.storyanvil.cogwheel.mixin;

import com.storyanvil.cogwheel.client.CutSceneManagement;
import com.storyanvil.cogwheel.data.CameraPos;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(Vec3 pPos);

    @Shadow protected abstract void setRotation(float pYRot, float pXRot);

    @Shadow public abstract Vec3 getPosition();

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();

    @Shadow protected abstract void setPosition(double pX, double pY, double pZ);

    @Shadow private boolean detached;

    @Inject(method = "setup", at = @At("RETURN"))
    public void update(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float particalTick, CallbackInfo callback) {
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            boolean detach = false;
            CameraPos force = CutSceneManagement.getPositionForce();
            if (force != null) {
                setPosition(force.getPos());
                setRotation(force.getRotY(), force.getRotX());
                detach = true;
                return;
            }

            Bi<CameraPos, CameraPos> transition = CutSceneManagement.getTransition();
            if (transition != null) {
                boolean upd = false;
                if (transition.getA() == null) {
                    transition.setA(new CameraPos(getPosition(), getYRot(), getXRot()));
                    upd = true;
                }
                if (transition.getB() == null) {
                    transition.setB(new CameraPos(getPosition(), getYRot(), getXRot()));
                    upd = true;
                }
                if (upd) {
                    CutSceneManagement.setTransition(transition);
                }
                float delta = CutSceneManagement.getTransitionDelta() + particalTick;
                CutSceneManagement.setTransitionDelta(delta);
                float progress = Mth.clamp((delta + 0.01f) / CutSceneManagement.getTransitionGoal(), 0f, 1f);
                Vec3 A = transition.getA().getPos();
                Vec3 B = transition.getB().getPos();
                setPosition(Mth.lerp(progress, A.x, B.x), Mth.lerp(progress, A.y, B.y), Mth.lerp(progress, A.z, B.z));
                setRotation(Mth.rotLerp(progress, transition.getA().getRotY(), transition.getB().getRotY()),
                        Mth.rotLerp(progress, transition.getA().getRotX(), transition.getB().getRotX()));
                detach = true;
            }

            this.detached |= detach;
            CutSceneManagement.setRenderHand(!detach);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
