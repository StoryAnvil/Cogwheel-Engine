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

package com.storyanvil.cogwheel.client.devui;

import java.util.function.Consumer;

public class DWConsoleChoice {
    private String mask;
    private String arguments;
    private String description;
    private Consumer<String> executor;

    public DWConsoleChoice(String mask, String arguments, String description, Consumer<String> executor) {
        this.mask = mask;
        this.arguments = " " + arguments;
        this.description = " - " + description;
        this.executor = executor;
    }

    public DWConsoleChoice(String mask, String description, Consumer<String> executor) {
        this(mask, "", description, executor);
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Consumer<String> getExecutor() {
        return executor;
    }

    public void setExecutor(Consumer<String> executor) {
        this.executor = executor;
    }
}
