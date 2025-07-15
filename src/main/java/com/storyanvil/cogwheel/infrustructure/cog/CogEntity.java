package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class CogEntity implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("entity", CogEntity::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("getEntityType", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(ForgeRegistries.ENTITY_TYPES.getResourceKey(e.e.getType()).get().location().toString());
        });
        manager.reg("getUUID", (name, args, script, o) -> {
            CogEntity e = (CogEntity) o;
            return new CogString(e.e.getStringUUID());
        });
        manager.reg("toEntity", (name, args, script, o) -> {
            return (CogEntity) o;
        });
    }

    private final Entity e;
    public CogEntity(Entity entity) {
        e = entity;
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
            return e.getUUID().equals(other.e.getUUID());
        }
        return false;
    }
}
