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

import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AnimationBound {
    private @NotNull String animatorID;
    private @NotNull String animation;

    public AnimationBound() {
        animatorID = "";
        animation = "";
    }

    public @NotNull String getAnimatorID() {
        return animatorID;
    }

    public @NotNull String getAnimation() {
        return animation;
    }

    public AnimationBound(@NotNull String animatorID, @NotNull String animation) {
        this.animatorID = animatorID;
        this.animation = animation;
    }

    public void encode(@NotNull FriendlyByteBuf friendlyByteBuf) {
        StoryUtils.encodeString(friendlyByteBuf, animatorID);
        StoryUtils.encodeString(friendlyByteBuf, animation);
    }

    public static @NotNull AnimationBound decode(FriendlyByteBuf friendlyByteBuf) {
        AnimationBound bound = new AnimationBound();
        bound.animatorID = StoryUtils.decodeString(friendlyByteBuf);
        bound.animation = StoryUtils.decodeString(friendlyByteBuf);
        return bound;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CogwheelClientPacketHandler.animationBound(this, ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
