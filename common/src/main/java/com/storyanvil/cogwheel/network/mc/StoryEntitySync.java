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

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.data.*;
import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntitySubscriber;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.storyanvil.cogwheel.util.CogwheelExecutor.log;

public record StoryEntitySync(String uuid, NbtCompound compound) implements StoryPacket<StoryEntitySync> {
    public static final StoryCodec<StoryEntitySync> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.String(StoryEntitySync::uuid),
            StoryCodecBldr.Prop(StoryEntitySync::compound, StoryCodecs.NBT_COMPOUND),
            StoryEntitySync::new
    );

    @Override
    public StoryCodec<StoryEntitySync> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        CogwheelExecutor.scheduleTickEventClientSide(level -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                log.warn("Failed to run data bound because LocalPlayer is NULL!");
                return;
            }
            BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
            int d = 1000;
            Box aabb = new Box(pos.south(d).east(d).down(d).toCenterPos(), pos.north(d).west(d).up(d).toCenterPos());
            List<Entity> e = level.getEntitiesByType(new TypeFilter<Entity, Entity>() {
                @Override
                public @Nullable Entity downcast(Entity obj) {
                    if (obj instanceof IStoryEntity npc) return obj;
                    return null;
                }

                @Override
                public Class<? extends Entity> getBaseClass() {
                    return Entity.class;
                }
            }, aabb, entity -> entity.getUuidAsString().equals(uuid));
            if (e.isEmpty()) {
                log.warn("Failed to run data bound for uuid {} and data {} | No IStoryEntity found", uuid(), compound());
                return;
            }
            for (Entity entity : e) {
                IStoryEntity storyEntity = (IStoryEntity) entity;
                storyEntity.storyEntity$set(compound);
                if (storyEntity instanceof IStoryEntitySubscriber subscriber)
                    subscriber.storyEntity$accept();
            }
        });
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        CogwheelExecutor.scheduleTickEvent(world -> {
            ServerPlayerEntity player = ctx.getSender();
            assert player != null;
            if (tryToCompleteServerSync(world, player)) {
                CogwheelExecutor.scheduleTickEvent(world0 -> {
                    if (tryToCompleteServerSync(world0, player)) {
                        CogwheelExecutor.scheduleTickEvent(world1 -> {
                            if (tryToCompleteServerSync(world1, player)) {
                                CogwheelHooks.sendPacket(player, new Notification(
                                        Text.literal("FATAL!"), Text.literal("Failed to sync npc " + uuid())
                                ));
                            }
                        }, 5);
                    }
                }, 5);
            }
        });
    }

    private boolean tryToCompleteServerSync(ServerWorld world, ServerPlayerEntity player) {
        BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        int d = 1000;
        Box aabb = new Box(pos.south(d).east(d).down(d).toCenterPos(), pos.north(d).west(d).up(d).toCenterPos());
        List<Entity> e = world.getEntitiesByType(new TypeFilter<Entity, Entity>() {
            @Override
            public @Nullable Entity downcast(Entity obj) {
                if (obj instanceof IStoryEntity npc) return obj;
                return null;
            }

            @Override
            public Class<? extends Entity> getBaseClass() {
                return Entity.class;
            }
        }, aabb, entity -> entity.getUuidAsString().equals(uuid));
        if (e.isEmpty()) {
            log.warn("Failed to run data bound request for uuid {} | No IStoryEntity found", uuid());
            return true;
        }
        for (Entity entity : e) {
            IStoryEntity storyEntity = (IStoryEntity) entity;
            CogwheelHooks.sendPacket(player, new StoryEntitySync(uuid(), storyEntity.storyEntity$get()));
        }
        return false;
    }
}
