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

import com.storyanvil.cogwheel.data.CameraPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CameraTransitionBound {
    private CameraPos pos1;
    private CameraPos pos2;
    private float goal;

    public CameraPos getPos1() {
        return pos1;
    }

    public void setPos1(CameraPos pos1) {
        this.pos1 = pos1;
    }

    public CameraPos getPos2() {
        return pos2;
    }

    public void setPos2(CameraPos pos2) {
        this.pos2 = pos2;
    }

    public float getGoal() {
        return goal;
    }

    public void setGoal(float goal) {
        this.goal = goal;
    }

    public CameraTransitionBound() {
    }

    public CameraTransitionBound(CameraPos pos1, CameraPos pos2, float goal) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.goal = goal;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        if (pos1 == null) {
            byteBuf.writeBoolean(false);
        } else {
            byteBuf.writeBoolean(true);
            pos1.encode(byteBuf);
        }
        if (pos2 == null) {
            byteBuf.writeBoolean(false);
        } else {
            byteBuf.writeBoolean(true);
            pos2.encode(byteBuf);
        }
        byteBuf.writeFloat(goal);
    }

    public static CameraTransitionBound decode(FriendlyByteBuf byteBuf) {
        CameraTransitionBound bound = new CameraTransitionBound();
        if (byteBuf.readBoolean()) {
            bound.pos1 = CameraPos.decode(byteBuf);
        } else {
            bound.pos1 = null;
        }
        if (byteBuf.readBoolean()) {
            bound.pos2 = CameraPos.decode(byteBuf);
        } else {
            bound.pos1 = null;
        }
        bound.goal = byteBuf.readFloat();
        return bound;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> CogwheelClientPacketHandler.cameraTrans(this, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    @Override
    public String toString() {
        return "CameraTransitionBound{" +
                "pos1=" + pos1 +
                ", pos2=" + pos2 +
                ", goal=" + goal +
                '}';
    }
}
