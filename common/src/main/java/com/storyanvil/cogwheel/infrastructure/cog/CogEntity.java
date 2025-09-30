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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CogEntity implements CGPM {
    private static final EasyPropManager MANAGER = new EasyPropManager("entity", CogEntity::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("getEntityType", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(Registries.ENTITY_TYPE.getId(unsafeEntity(e).getType()).toString());
        });
        manager.reg("getUUID", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(unsafeEntity(e).getUuidAsString());
        });
        manager.reg("toEntity", (name, args, script, o) -> {
            return (CogEntity) o;
        });
        manager.reg("teleport", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            unsafeEntity(e).requestTeleport(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1), args.requireDoubleOrInt(2));
            return e;
        });
        manager.reg("turn", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            unsafeEntity(e).rotate((float) args.requireDoubleOrInt(0), (float) args.requireDoubleOrInt(1));
            return e;
        });
        manager.reg("teleport2", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            unsafeEntity(e).requestTeleport(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1), args.requireDoubleOrInt(2));
            unsafeEntity(e).rotate((float) args.requireDoubleOrInt(3), (float) args.requireDoubleOrInt(4));
            return e;
        });
        manager.reg("getX", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(unsafeEntity(e).getX());
        });
        manager.reg("getY", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(unsafeEntity(e).getY());
        });
        manager.reg("getZ", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(unsafeEntity(e).getZ());
        });
        manager.reg("getBlockX", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(unsafeEntity(e).getBlockX());
        });
        manager.reg("getBlockY", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(unsafeEntity(e).getBlockY());
        });
        manager.reg("getBlockZ", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(unsafeEntity(e).getBlockZ());
        });
        manager.reg("kill", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            unsafeEntity(e).kill((ServerWorld) unsafeEntity(e).getWorld());
            e.e.clear();
            return null;
        });
        manager.reg("hasTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(unsafeEntity(e).getCommandTags().contains(args.getString(0)));
        });
        manager.reg("addTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(unsafeEntity(e).addCommandTag(args.getString(0)));
        });
        manager.reg("removeTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(unsafeEntity(e).removeCommandTag(args.getString(0)));
        });
        manager.reg("getTags", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogArray.convertInstance(unsafeEntity(e).getCommandTags());
        });
        manager.reg("putInt", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setInt(unsafeEntity(e), args.getString(0), args.requireInt(1));
            return e;
        });
        manager.reg("getInt", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(DataStorage.getInt(unsafeEntity(e), args.getString(0), 0));
        });
        manager.reg("putString", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setString(unsafeEntity(e), args.getString(0), args.getString(1));
            return e;
        });
        manager.reg("getString", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(DataStorage.getString(unsafeEntity(e), args.getString(0), ""));
        });
        manager.reg("putBoolean", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setBoolean(unsafeEntity(e), args.getString(0), args.requireBoolean(1));
            return e;
        });
        manager.reg("getBoolean", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(DataStorage.getBoolean(unsafeEntity(e), args.getString(0), false));
        });
        manager.reg("putLong", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setString(unsafeEntity(e), args.getString(0), String.valueOf(args.requireLong(1)));
            return e;
        });
        manager.reg("getLong", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogLong(DataStorage.getString(unsafeEntity(e), args.getString(0), "0"));
        });
        manager.reg("getDisplayName", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(Objects.requireNonNull(Objects.requireNonNull(e.e.get(), "entity got unloaded"), "entity got unloaded").getDisplayName().getString());
        });
    }

    private static @NotNull Entity unsafeEntity(CogEntity e) {
        return Objects.requireNonNull(e.e.get(), "entity got unloaded");
    }

    private final WeakReference<Entity> e;
    @Api.Experimental(since = "2.0.0")
    public CogEntity(WeakReference<Entity> entity) {
        e = entity;
    }
    @Api.Stable(since = "2.0.0")
    public CogEntity(Entity entity) {
        this(new WeakReference<>(entity));
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws CogScriptException {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        if (o instanceof CogEntity other) {
            return e.get() == other.e.get();
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(e);
    }
}
