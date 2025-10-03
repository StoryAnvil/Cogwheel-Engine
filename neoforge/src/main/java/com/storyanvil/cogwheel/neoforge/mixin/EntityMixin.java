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

package com.storyanvil.cogwheel.neoforge.mixin;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import com.storyanvil.cogwheel.neoforge.NeoRegistry;
import com.storyanvil.cogwheel.neoforge.data.NbtAttachment;
import com.storyanvil.cogwheel.network.mc.StoryEntitySync;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin extends AttachmentHolder implements IStoryEntity {
    @Shadow public abstract String getUuidAsString();

    @Override
    public IStoryEntity storyEntity$putInt(String k, int v) {
        NbtAttachment a = getData(NeoRegistry.DATA.get());
        a.getCompound().putInt(k, v);
        setData(NeoRegistry.DATA.get(), a);
        return this;
    }

    @Override
    public IStoryEntity storyEntity$putString(String k, String v) {
        NbtAttachment a = getData(NeoRegistry.DATA.get());
        a.getCompound().putString(k, v);
        setData(NeoRegistry.DATA.get(), a);
        return this;
    }

    @Override
    public IStoryEntity storyEntity$putBoolean(String k, boolean v) {
        NbtAttachment a = getData(NeoRegistry.DATA.get());
        a.getCompound().putBoolean(k, v);
        setData(NeoRegistry.DATA.get(), a);
        return this;
    }

    @Override
    public int storyEntity$getInt(String k, int defaultV) {
        NbtCompound c = getData(NeoRegistry.DATA.get()).getCompound();
        return c.contains(k) ? c.getInt(k) : defaultV;
    }

    @Override
    public String storyEntity$getString(String k, String defaultV) {
        NbtCompound c = getData(NeoRegistry.DATA.get()).getCompound();
        return c.contains(k) ? c.getString(k) : defaultV;
    }

    @Override
    public boolean storyEntity$getBoolean(String k, boolean defaultV) {
        NbtCompound c = getData(NeoRegistry.DATA.get()).getCompound();
        return c.contains(k) ? c.getBoolean(k) : defaultV;
    }

    @Override
    public NbtCompound storyEntity$get() {
        return getData(NeoRegistry.DATA.get()).getCompound();
    }

    @Override
    public void storyEntity$set(NbtCompound c) {
        setData(NeoRegistry.DATA.get(), new NbtAttachment(c));
    }

    @Override
    public void storyEntity$syncIfOnServer() {
        CogwheelHooks.sendPacketToEveryone(new StoryEntitySync(getUuidAsString(), storyEntity$get()));
    }
}
