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

package com.storyanvil.cogwheel.classgather;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.storyanvil.cogwheel.CogwheelEngine;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DiffJson {
    private final JsonObject json;
    private boolean valid = true;
    private DiffJson parent = null;

    private DiffJson(JsonObject json) {
        this.json = json;
    }

    // ========== === [ Factories ] === ========== \\
    public static @NotNull DiffJson readFromFile(File file) {
        try (FileReader fr = new FileReader(file)) {
            return DiffJson.fromElement(JsonParser.parseReader(fr));
        } catch (IOException e) {
            CogwheelEngine.LOGGER.error("Failed to read DiffJson [file]", e);
            return new DiffJson(new JsonObject());
        }
    }
    @Contract("_ -> new")
    public static @NotNull DiffJson fromElement(JsonElement element) {
        try {
            return new DiffJson(element.getAsJsonObject());
        } catch (Exception e) {
            CogwheelEngine.LOGGER.error("Failed to create DiffJson [element]", e);
            return new DiffJson(new JsonObject());
        }
    }

    // ========== === [ Methods ] === ========== \\

    /**
     * Makes sure that value in provided key is equal to provided value. If this is not true, fixes this by creating diff.
     * @param key Key to check
     * @param data Correct data for provided key
     */
    public void checkValue(String key, JsonElement data) {
        if (!json.has(key)) {
            json.add("+" + key, data);
            invalidate();
            return;
        }
        if (!json.get(key).equals(data)) {
            json.add("-" + key, json.get(key));
            json.add("+" + key, data);
            invalidate();
        }
    }

    /**
     * Calls {@link DiffJson#checkValue(String, JsonElement)} with data converted to JsonElement
     */
    public void checkValueString(String key, String data) {
        checkValue(key, new JsonPrimitive(data));
    }

    /**
     * Calls {@link DiffJson#checkValue(String, JsonElement)} with data converted to JsonElement
     */
    public void checkValueInt(String key, int data) {
        checkValue(key, new JsonPrimitive(data));
    }

    /**
     * Calls {@link DiffJson#checkValue(String, JsonElement)} with data converted to JsonElement
     */
    public void checkValueBoolean(String key, boolean data) {
        checkValue(key, new JsonPrimitive(data));
    }

    /**
     * Makes sure that value in provided key is a string. If this is not true creates diff that sets provided key to provided default value
     * @param key Key to check
     * @param defaultValue Default value that will be used if object stores invalid value
     */
    public void checkString(String key, String defaultValue) {
        if (!json.has(key)) {
            json.addProperty("+" + key, defaultValue);
            invalidate();
            return;
        }
        JsonElement e = json.get(key);
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
            json.add("-" + key, e);
            json.addProperty("+" + key, defaultValue);
            invalidate();
            return;
        }
    }

    /**
     * Makes sure that value in provided key is a JsonArray. If this is not true creates diff that sets provided key to provided default value
     * @param key Key to check
     * @param defaultValue Default value that will be used if object stores invalid value
     */
    public void checkArray(String key, JsonArray defaultValue) {
        if (!json.has(key)) {
            json.add("+" + key, defaultValue);
            invalidate();
            return;
        }
        JsonElement e = json.get(key);
        if (!e.isJsonArray()) {
            json.add("-" + key, e);
            json.add("+" + key, defaultValue);
            invalidate();
            return;
        }
    }

    /**
     * Makes sure that value in provided key is an integer. If this is not true creates diff that sets provided key to provided default value
     * @param key Key to check
     * @param defaultValue Default value that will be used if object stores invalid value
     */
    public void checkInt(String key, int defaultValue) {
        if (!json.has(key)) {
            json.addProperty("+" + key, defaultValue);
            invalidate();
            return;
        }
        JsonElement e = json.get(key);
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) {
            json.add("-" + key, e);
            json.addProperty("+" + key, defaultValue);
            invalidate();
            return;
        }
    }

    /**
     * Makes sure that value in provided key is a boolean. If this is not true creates diff that sets provided key to provided default value
     * @param key Key to check
     * @param defaultValue Default value that will be used if object stores invalid value
     */
    public void checkBoolean(String key, boolean defaultValue) {
        if (!json.has(key)) {
            json.addProperty("+" + key, defaultValue);
            invalidate();
            return;
        }
        JsonElement e = json.get(key);
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean()) {
            json.add("-" + key, e);
            json.addProperty("+" + key, defaultValue);
            invalidate();
            return;
        }
    }

    /**
     * Makes sure that value in provided key is validated {@link DiffJson}.
     * @param key Key to check
     * @param defaultValue Default value that will be used if object has provided key unset
     * @return {@link DiffJson} instance linked to this {@link DiffJson}. Use this to check object stored in provided key
     */
    public DiffJson checkDiffJsonObj(String key, JsonObject defaultValue) {
        if (!json.has(key)) {
            json.add("+" + key, defaultValue);
            invalidate();
            return new DiffJson(defaultValue).withParent(this);
        }
        JsonElement e = json.get(key);
        if (!e.isJsonObject()) {
            json.add("-" + key, e);
            json.add("+" + key, defaultValue);
            invalidate();
            return new DiffJson(defaultValue).withParent(this);
        }
        return new DiffJson(e.getAsJsonObject()).withParent(this);
    }

    /**
     * Makes sure that provided key is unset in this object. Otherwise, creates diff that unsets this key
     * @param key Key to check
     */
    public void checkUnset(String key) {
        if (json.has(key)) {
            json.add("-" + key, json.get(key));
            json.remove(key);
            invalidate();
            return;
        }
    }

    /**
     * @return true if all of previous checks were successful and no diff were created
     */
    public boolean isStillValid() {
        return valid;
    }

    /**
     * Makes this object not valid and all its parents.
     */
    public void invalidate() {
        if (!valid) return;
        valid = false;
        if (parent != null) {
            parent.invalidate();
        }
    }

    private DiffJson withParent(@NotNull DiffJson parent) {
        this.parent = parent;
        if (!parent.valid)
            this.valid = false;
        return this;
    }

    public void writeToFile(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                CogwheelEngine.LOGGER.error("Failed to create {} to write DiffJSON", file);
            }
        }
        try (FileWriter fw = new FileWriter(file);JsonWriter writer = new JsonWriter(fw);) {
            writer.setStrictness(Strictness.LENIENT);
            writer.setFormattingStyle(FormattingStyle.PRETTY);
            Streams.write(json, writer);
            fw.flush();
        }
    }
    public void writeToFileCompact(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                CogwheelEngine.LOGGER.error("Failed to create {} to write compact DiffJSON", file);
            }
        }
        try (FileWriter fw = new FileWriter(file);JsonWriter writer = new JsonWriter(fw);) {
            writer.setStrictness(Strictness.LENIENT);
            writer.setFormattingStyle(FormattingStyle.COMPACT);
            Streams.write(json, writer);
            fw.flush();
        }
    }
}
