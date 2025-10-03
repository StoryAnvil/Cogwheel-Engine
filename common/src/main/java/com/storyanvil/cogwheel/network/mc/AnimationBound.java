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

import com.storyanvil.cogwheel.data.*;
import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.storyanvil.cogwheel.util.CogwheelExecutor.log;

public record AnimationBound(Identifier animatorID, String animation) implements StoryPacket<AnimationBound> {
    public static final StoryCodec<AnimationBound> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Prop(AnimationBound::animatorID, StoryCodecs.IDENTIFIER),
            StoryCodecBldr.String(AnimationBound::animation),
            AnimationBound::new
    );

    @Override
    public StoryCodec<AnimationBound> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        CogwheelExecutor.scheduleTickEventClientSide(level -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                log.warn("Failed to run animation bound because LocalPlayer is NULL!");
                return;
            }
            BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
            int d = 1000;
            Box aabb = new Box(pos.south(d).east(d).down(d).toCenterPos(), pos.north(d).west(d).up(d).toCenterPos());
            List<Entity> e = level.getEntitiesByType(new TypeFilter<Entity, Entity>() {
                @Override
                public @Nullable Entity downcast(Entity obj) {
                    if (obj instanceof AbstractNPC<?> npc) return obj;
                    return null;
                }

                @Override
                public Class<? extends Entity> getBaseClass() {
                    return Entity.class;
                }
            }, aabb, entity -> ((AbstractNPC<?>) entity).npc$equalsCheckForAnimatorID(animatorID()));
            if (e.isEmpty()) {
                log.warn("Failed to run animation bound for {}->{} | No AbstractNPCs found", animatorID(), animation());
                return;
            }
            for (Entity entity : e) {
                AbstractNPC<?> animator = (AbstractNPC<?>) entity;
                animator.npc$pushAnimation(animation());
            }
        });
    }
}
