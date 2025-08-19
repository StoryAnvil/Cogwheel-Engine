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
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Reference;

/**
 * ObjectMonitor used for debugging purposes and finding memory leaks
 */
@Api.Internal @ApiStatus.Internal
public class ObjectMonitor<T extends ObjectMonitor.IMonitored> {
    private final static WeakList<ObjectMonitor<?>> MONITOR_REGISTRY = new WeakList<>();
    private final static boolean ENABLED = true;

    private static synchronized int register(ObjectMonitor<?> monitor) {
        if (!ENABLED) return 0;
        int id = MONITOR_REGISTRY.size();
        MONITOR_REGISTRY.add(monitor);
        return id;
    }
    @Api.Internal @ApiStatus.Internal
    public static synchronized void dumpAll(StringBuilder sb) {
        if (!ENABLED) return;
        try {
            // For each is not supported by WeakList
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < MONITOR_REGISTRY.size(); i++) {
                ObjectMonitor<?> monitor = MONITOR_REGISTRY.get(i);
                if (monitor == null) {sb.append("=NULL MONITOR=\n");continue;}
                monitor.dump(sb);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private final WeakList<T> objects;
    private final int id;

    @Api.Internal @ApiStatus.Internal
    public ObjectMonitor() {
        if (!ENABLED) return;
        objects = new WeakList<>();
        id = register(this);
    }

    @Api.Internal @ApiStatus.Internal
    public void register(T object) {
        if (!ENABLED) return;
        if (objects.contains(object)) return;
        objects.add(object);
    }

    @Api.Internal @ApiStatus.Internal
    public String dump() {
        StringBuilder sb = new StringBuilder();
        dump(sb);
        return sb.toString();
    }

    @Api.Internal @ApiStatus.Internal
    public void dump(StringBuilder sb) {
        if (!ENABLED) return;
        sb.append("=== === === OBJECT MONITOR REPORT === === ===\n");
        sb.append("MONITOR ID: ").append(id).append("\nOBJECTS:\n");
        for (int i = 0; i < objects.size(); i++) {
            Reference<T> ref = objects.getRef(i);
            if (ref.refersTo(null)) {
                sb.append("    NULL\n");
                continue;
            }
            T t = ref.get();
            if (t == null) {
                sb.append("    UNEXPECTED NULL\n");
                continue;
            }
            sb.append("    ").append(t.getClass().getCanonicalName()).append(": ");
            t.reportState(sb);
            sb.append('\n');
        }
    }

    @Api.Internal @ApiStatus.Internal
    public WeakList<T> getObjects() {
        return objects;
    }

    public interface IMonitored {
        void reportState(StringBuilder sb);
    }
}
