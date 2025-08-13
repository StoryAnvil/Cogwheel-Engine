package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CogEntity implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("entity", CogEntity::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("getEntityType", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(ForgeRegistries.ENTITY_TYPES.getResourceKey(e.e.get().getType()).get().location().toString());
        });
        manager.reg("getUUID", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(e.e.get().getStringUUID());
        });
        manager.reg("toEntity", (name, args, script, o) -> {
            return (CogEntity) o;
        });
        manager.reg("teleport", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            e.e.get().teleportToWithTicket(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1), args.requireDoubleOrInt(2));
            return e;
        });
        manager.reg("turn", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            e.e.get().turn(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1));
            return e;
        });
        manager.reg("teleport2", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            e.e.get().teleportToWithTicket(args.requireDoubleOrInt(0), args.requireDoubleOrInt(1), args.requireDoubleOrInt(2));
            e.e.get().turn(args.requireDoubleOrInt(3), args.requireDoubleOrInt(4));
            return e;
        });
        manager.reg("getX", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(e.e.get().getX());
        });
        manager.reg("getY", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(e.e.get().getY());
        });
        manager.reg("getZ", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogDouble(e.e.get().getZ());
        });
        manager.reg("getBlockX", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(e.e.get().getBlockX());
        });
        manager.reg("getBlockY", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(e.e.get().getBlockY());
        });
        manager.reg("getBlockZ", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(e.e.get().getBlockZ());
        });
        manager.reg("kill", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            e.e.get().kill();
            e.e.clear();
            return null;
        });
        manager.reg("hasTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(e.e.get().getTags().contains(args.getString(0)));
        });
        manager.reg("addTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(e.e.get().getTags().add(args.getString(0)));
        });
        manager.reg("removeTag", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(e.e.get().getTags().remove(args.getString(0)));
        });
        manager.reg("getTags", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogArray.convertInstance(e.e.get().getTags());
        });
        manager.reg("putInt", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setInt(e.e.get(), args.getString(0), args.requireInt(1));
            return e;
        });
        manager.reg("getInt", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogInteger(DataStorage.getInt(e.e.get(), args.getString(0), 0));
        });
        manager.reg("putString", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setString(e.e.get(), args.getString(0), args.getString(1));
            return e;
        });
        manager.reg("getString", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(DataStorage.getString(e.e.get(), args.getString(0), ""));
        });
        manager.reg("putBoolean", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            DataStorage.setBoolean(e.e.get(), args.getString(0), args.requireBoolean(1));
            return e;
        });
        manager.reg("getBoolean", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return CogBool.getInstance(DataStorage.getBoolean(e.e.get(), args.getString(0), false));
        });
        manager.reg("getDisplayName", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(e.e.get().getDisplayName().getString());
        });
    }

    private final WeakReference<Entity> e;
    public CogEntity(WeakReference<Entity> entity) {
        e = entity;
    }
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
            return e.get().getUUID().equals(other.e.get().getUUID());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(e);
    }
}
