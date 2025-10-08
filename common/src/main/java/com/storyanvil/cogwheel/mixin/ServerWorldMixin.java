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

import com.storyanvil.cogwheel.mixinAccess.IStoryEventScheduler;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.EntityLookupView;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Vector;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements IStoryEventScheduler<Consumer<ServerWorld>>, EntityLookupView, StructureWorldAccess {
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Unique
    private final Vector<Task<Consumer<ServerWorld>>> storyEvent$tasks = new Vector<>(1);

    @Inject(at = @At("TAIL"), method = "tick")
    public void tickMixin(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        Profiler profiler = Profilers.get();
        profiler.push("CogwheelEngine: World Tasks");
        synchronized (storyEvent$tasks) {
            for (int i = 0; i < storyEvent$tasks.size(); i++) {
                if (storyEvent$tasks.get(i).decrement()) {
                    storyEvent$tasks.remove(i).getEvent().accept((ServerWorld) (Object) this /* just don't question this cast */);
                    i--;
                }
            }
        }
        profiler.pop();
    }

    @Override
    public void storyEvent$scheduleEvent(Consumer<ServerWorld> event) {
        synchronized (storyEvent$tasks) {
            storyEvent$tasks.add(new Task<>(event));
        }
    }

    @Override
    public void storyEvent$scheduleEvent(Consumer<ServerWorld> event, int loops) {
        synchronized (storyEvent$tasks) {
            storyEvent$tasks.add(new Task<>(event, loops));
        }
    }
}
