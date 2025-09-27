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

package com.storyanvil.cogwheel.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public class CameraPos {
    private Vec3d pos;
    private float rotY;
    private float rotX;

    public CameraPos(Vec3d pos, float rotY, float rotX) {
        this.pos = pos;
        this.rotY = rotY;
        this.rotX = rotX;
    }
    public CameraPos() {
        this.pos = new Vec3d(0, 0, 0);
        this.rotY = 0;
        this.rotX = 0;
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    @Override
    public String toString() {
        return "CameraPos{" +
                "pos=" + pos +
                ", rotY=" + rotY +
                ", rotX=" + rotX +
                '}';
    }

    public void encode(PacketByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeFloat(rotX);
        buf.writeFloat(rotY);
    }
    public static CameraPos decode(PacketByteBuf buf) {
        CameraPos pos = new CameraPos();
        pos.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        pos.rotX = buf.readFloat();
        pos.rotY = buf.readFloat();
        return pos;
    }
}
