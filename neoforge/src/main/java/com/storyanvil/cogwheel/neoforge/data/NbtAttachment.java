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

package com.storyanvil.cogwheel.neoforge.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class NbtAttachment implements INBTSerializable<NbtCompound> {
    private NbtCompound compound;

    public NbtAttachment() {
        this.compound = new NbtCompound();
    }

    public NbtAttachment(NbtCompound compound) {
        this.compound = compound;
    }

    public NbtCompound getCompound() {
        return compound;
    }

    public void setCompound(NbtCompound compound) {
        this.compound = compound;
    }

    @Override
    public @UnknownNullability NbtCompound serializeNBT(RegistryWrapper.@NotNull WrapperLookup arg) {
        return compound;
    }

    @Override
    public void deserializeNBT(RegistryWrapper.@NotNull WrapperLookup arg, @NotNull NbtCompound arg2) {
        compound = arg2;
    }
}
