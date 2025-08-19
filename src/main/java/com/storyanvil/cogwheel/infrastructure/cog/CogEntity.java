package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.DispatchedScript;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CogEntity implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("entity", CogEntity::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("getEntityType", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            //noinspection OptionalGetWithoutIsPresent
            return new CogString(ForgeRegistries.ENTITY_TYPES.getResourceKey(Objects.requireNonNull(e.e.get(), "entity got unloaded").getType()).get().location().toString());
        });
        manager.reg("getUUID", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(Objects.requireNonNull(e.e.get(), "entity got unloaded").getStringUUID());
        });
        manager.reg("toEntity", (name, args, script, o) -> {
            return (CogEntity) o;
        });
        manager.reg("teleport", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            Objects.requireNonNull(e.e.get(), "entity got unloaded").teleportToWithTicket(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1), args.requireDoubleOrInt(2));
            return e;
        });
        manager.reg("turn", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            Objects.requireNonNull(e.e.get(), "entity got unloaded").turn(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1));
            return e;
        });
        manager.reg("teleport2", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            Objects.requireNonNull(e.e.get(), "entity got unloaded").teleportToWithTicket(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1), args.requireDoubleOrInt(2));
            Objects.requireNonNull(e.e.get(), "entity got unloaded").turn(args.requireDoubleOrInt(3), args.requireDoubleOrInt(4));
            return e;
        });
        manager.reg("getX", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(Objects.requireNonNull(e.e.get(), "entity got unloaded").getX());
        });
        manager.reg("getY", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(Objects.requireNonNull(e.e.get(), "entity got unloaded").getY());
        });
        manager.reg("getZ", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(Objects.requireNonNull(e.e.get(), "entity got unloaded").getZ());
        });
        manager.reg("getBlockX", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(Objects.requireNonNull(e.e.get(), "entity got unloaded").getBlockX());
        });
        manager.reg("getBlockY", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(Objects.requireNonNull(e.e.get(), "entity got unloaded").getBlockY());
        });
        manager.reg("getBlockZ", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(Objects.requireNonNull(e.e.get(), "entity got unloaded").getBlockZ());
        });
        manager.reg("kill", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            Objects.requireNonNull(e.e.get(), "entity got unloaded").kill();
            e.e.clear();
            return null;
        });
        manager.reg("hasTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(Objects.requireNonNull(e.e.get(), "entity got unloaded").getTags().contains(args.getString(0)));
        });
        manager.reg("addTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(Objects.requireNonNull(e.e.get(), "entity got unloaded").getTags().add(args.getString(0)));
        });
        manager.reg("removeTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(Objects.requireNonNull(e.e.get(), "entity got unloaded").getTags().remove(args.getString(0)));
        });
        manager.reg("getTags", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogArray.convertInstance(Objects.requireNonNull(e.e.get(), "entity got unloaded").getTags());
        });
        manager.reg("putInt", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setInt(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), args.requireInt(1));
            return e;
        });
        manager.reg("getInt", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(DataStorage.getInt(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), 0));
        });
        manager.reg("putString", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setString(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), args.getString(1));
            return e;
        });
        manager.reg("getString", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(DataStorage.getString(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), ""));
        });
        manager.reg("putBoolean", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setBoolean(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), args.requireBoolean(1));
            return e;
        });
        manager.reg("getBoolean", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(DataStorage.getBoolean(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), false));
        });
        manager.reg("putLong", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setString(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), String.valueOf(args.requireLong(1)));
            return e;
        });
        manager.reg("getLong", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogLong(DataStorage.getString(Objects.requireNonNull(e.e.get(), "entity got unloaded"), args.getString(0), "0"));
        });
        manager.reg("getDisplayName", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(Objects.requireNonNull(Objects.requireNonNull(e.e.get(), "entity got unloaded"), "entity got unloaded").getDisplayName().getString());
        });
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
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
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
