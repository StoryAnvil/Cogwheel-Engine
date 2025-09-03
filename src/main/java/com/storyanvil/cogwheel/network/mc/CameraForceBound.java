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
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CameraForceBound {
    private CameraPos pos;

    public CameraPos getPos() {
        return pos;
    }

    public CameraForceBound(CameraPos pos) {
        this.pos = pos;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        if (pos == null) {
            byteBuf.writeBoolean(false);
        } else {
            byteBuf.writeBoolean(true);
            pos.encode(byteBuf);
        }
    }

    public static CameraForceBound decode(FriendlyByteBuf byteBuf) {
        CameraForceBound bound = new CameraForceBound(null);
        if (byteBuf.readBoolean()) {
            bound.pos = CameraPos.decode(byteBuf);
        }
        return bound;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> CogwheelClientPacketHandler.cameraForce(this, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
