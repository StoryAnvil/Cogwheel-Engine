/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.cog.CogInvoker;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.CogBool;
import com.storyanvil.cogwheel.infrastructure.cog.PropertyHandler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Api.Internal @ApiStatus.Internal
public class EasyPropManager {
    public static final HashMap<String, PropertyHandler> allHandlers = new HashMap<>();

    static {
        defaultMethods(allHandlers);
    }

    public static void defaultMethods(@NotNull HashMap<String, PropertyHandler> handlers) {
        handlers.put(PropertyLocation.fromNamespaceAndPath("any", "toString"), (name, args, script, o) -> {
            return ((CogPropertyManager) o).convertToCogString();
        });
        handlers.put(PropertyLocation.fromNamespaceAndPath("any", "equals"), (name, args, script, o) -> {
            return CogBool.getInstance(((CogPropertyManager) o).equalsTo(args.get(0)));
        });
        handlers.put(PropertyLocation.fromNamespaceAndPath("any", "asInvoker"), (name, args, script, o) -> {
            CogPropertyManager m = (CogPropertyManager) o;
            return CogInvoker.genericInvoker(m, args.getString(0));
        });
    }

    private final String manager;
    private final WeakReference<HashMap<String, PropertyHandler>> handlers;

    @Api.Internal @ApiStatus.Internal
    public EasyPropManager(String manager, @NotNull Registrar registrar) {
        this.manager = manager;
        this.handlers = new WeakReference<>(allHandlers);
        registrar.register(this);
    }

    @Api.Internal @ApiStatus.Internal
    public EasyPropManager(String manager, @NotNull Registrar registrar, @NotNull HashMap<String, PropertyHandler> h) {
        this.manager = manager;
        this.handlers = new WeakReference<>(h);
        registrar.register(this);
    }

    @Api.Internal @ApiStatus.Internal
    public boolean hasOwnProperty(String name) {
        //noinspection DataFlowIssue
        return this.handlers.get().containsKey(PropertyLocation.fromNamespaceAndPath(this.manager, name)) || this.handlers.get().containsKey(PropertyLocation.fromNamespaceAndPath("any", name));
    }

    @Api.Internal @ApiStatus.Internal
    public boolean hasOwnProperty(String name, Function<String,Boolean> alt) {
        return this.hasOwnProperty(name) || alt.apply(name);
    }

    @Api.Internal @ApiStatus.Internal
    public PropertyHandler get(String name) {
        PropertyHandler h = this.handlers.get().get(PropertyLocation.fromNamespaceAndPath(manager, name));
        if (h != null) return h;
        return this.handlers.get().get(PropertyLocation.fromNamespaceAndPath("any", name));
    }
    @Api.Internal @ApiStatus.Internal
    public PropertyHandler get(String name, Function<String,PropertyHandler> alt) {
        PropertyHandler h = this.get(name);
        if (h != null) return h;
        return alt.apply(name);
    }
    @Api.Internal @ApiStatus.Internal
    public CogPropertyManager get(String name, ArgumentData args, DispatchedScript script, Object o) {
        PropertyHandler h = this.get(name);
        return h.handle(name, args, script, o);
    }
    @Api.Internal @ApiStatus.Internal
    public CogPropertyManager get(String name, ArgumentData args, DispatchedScript script, Object o, Supplier<CogPropertyManager> alt) {
        PropertyHandler h = this.get(name);
        if (h != null) return h.handle(name, args, script, o);
        return alt.get();
    }

    @Api.Internal @ApiStatus.Internal
    public void reg(String name, PropertyHandler handler) {
        handlers.get().put(PropertyLocation.fromNamespaceAndPath(manager, name), handler);
    }

//    public static void dispose(String manager) {
//        for (Map.Entry<ResourceLocation, PropertyHandler> handlerEntry : Set.copyOf(handlers.entrySet())) {
//            if (handlerEntry.getKey().getNamespace().equals(manager)) {
//                handlers.remove(handlerEntry.getKey());
//            }
//        }
//    }
//    public static void dispose(Predicate<String> remove) {
//        for (Map.Entry<ResourceLocation, PropertyHandler> handlerEntry : Set.copyOf(handlers.entrySet())) {
//            if (remove.test(handlerEntry.getKey().getNamespace())) {
//                handlers.remove(handlerEntry.getKey());
//            }
//        }
//    }

    @Api.Internal @ApiStatus.Internal
    public interface Registrar {
        @Api.Internal @ApiStatus.Internal
        void register(EasyPropManager manager);
    }
}
