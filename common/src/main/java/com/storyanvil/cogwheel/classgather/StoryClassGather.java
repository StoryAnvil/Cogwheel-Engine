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

/*package com.storyanvil.cogwheel.classgather;

import com.storyanvil.cogwheel.util.WrapperException;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

public class StoryClassGather {
    private StoryClassGather() {}

    public static StoryClassGather builder() {
        return new StoryClassGather();
    }

    private ArrayList<Predicate<Class<?>>> filters = new ArrayList<>();
    private ArrayList<Predicate<String>> earlyFilters = new ArrayList<>();
    private ArrayList<URLClassLoader> classLoaders = new ArrayList<>();

    public StoryClassGather subTypesOf(Class<?> clazz) {
        filters.add(new Filters.SubTypes(clazz));
        return this;
    }

    public StoryClassGather addStaticClassLoader() {
        try {
            classLoaders.add((URLClassLoader) StoryClassGather.class.getClassLoader());
        } catch (ClassCastException e) {
            throw new WrapperException("StaticClassLoader is not a URLClassLoader", e);
        }
        return this;
    }
    public StoryClassGather addContextClassLoader() {
        try {
            classLoaders.add((URLClassLoader) Thread.currentThread().getContextClassLoader());
        } catch (ClassCastException e) {
            throw new WrapperException("ContextClassLoader is not a URLClassLoader", e);
        }
        return this;
    }
    public StoryClassGather addURLClassLoader(ClassLoader loader) {
        try {
            classLoaders.add((URLClassLoader) loader);
        } catch (ClassCastException e) {
            throw new WrapperException("Custom ClassLoader is not a URLClassLoader", e);
        }
        return this;
    }

    public Iterable<Class<?>> gather() {
        HashMap<String, URL> distinctUrls = new HashMap<>();
        for (URLClassLoader loader : classLoaders) {
            URLClassLoader currentLoader = loader;
            while (currentLoader != null) {
                for (URL url : currentLoader.getURLs()) {
                    distinctUrls.put(url.toExternalForm(), url);
                }
                ClassLoader cl = currentLoader.getParent();
                if (cl instanceof URLClassLoader l)
                    currentLoader = l;
                else currentLoader = null;
            }
        }
        return null;
    }

    public static class Filters {
        public static class SubTypes implements Predicate<Class<?>> {
            private final Class<?> type;

            public SubTypes(Class<?> type) {
                this.type = type;
            }

            @Override
            public boolean test(Class<?> aClass) {
                try {
                    aClass.asSubclass(type);
                    return true;
                } catch (ClassCastException e) {
                    return false;
                }
            }
        }
    }
}
*/