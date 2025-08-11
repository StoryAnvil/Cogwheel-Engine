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

package com.storyanvil.cogwheel.infrustructure.actions;

import com.storyanvil.cogwheel.infrustructure.StoryAction;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class LookAtAction extends StoryAction<Entity> {
    private Vec3 target;
    private float xRot;
    private float yRot;
    private float yHeadRot;
    private float delta = 0f;

    public LookAtAction(Vec3 target) {
        this.target = target;
    }

    @Override
    public void proceed(Entity myself) {

        Vec3 vec3 = EntityAnchorArgument.Anchor.EYES.apply(myself);
        double d0 = target.x - vec3.x;
        double d1 = target.y - vec3.y;
        double d2 = target.z - vec3.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        xRot = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
        yRot = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
        yHeadRot = myself.getYRot();
    }

    @Override
    public boolean freeToGo(Entity myself) {
        delta += 0.1f;
        delta = Math.min(delta, 1f);
        myself.setXRot(Mth.rotLerp(delta, myself.getXRot(), xRot));
        myself.setYRot(Mth.rotLerp(delta, myself.getYRot(), yRot));
        myself.setYHeadRot(Mth.lerp(delta, myself.getYHeadRot(), yHeadRot));
        myself.xRotO = myself.getXRot();
        myself.yRotO = myself.getYRot();
        return delta >= 1f;
    }
}
