/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.infrastructure.module;

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.PropertyHandler;
import com.storyanvil.cogwheel.util.CogExpressionFailure;

import java.util.ArrayList;
import java.util.Arrays;

public class ModuleProperty implements PropertyHandler {
    private ArrayList<String> $lines;
    private CogModule $parent;
    private String[] $arguments;

    public ModuleProperty(ArrayList<String> lines, CogModule parent, String[] arguments) {
        this.$lines = lines;
        this.$parent = parent;
        this.$arguments = arguments;
    }

    @Override
    public CogPropertyManager handle(String name, ArgumentData arg, DispatchedScript script, Object o) {
        if (o == null) {
            o = new CMA($parent);
        }
        CMA instance = (CMA) o;
        DispatchedScript $ = new DispatchedScript((ArrayList<String>) $lines.clone(), $parent.getEnvironment()).setScriptName("PROPERTY");
        $.put("this", instance);
        CogPropertyManager[] args = arg.getArgs();
        if (args.length != $arguments.length) throw new CogExpressionFailure("Invalid amount of arguments {" + args.length + "} of {" + $arguments.length + "}");
        for (int i = 0; i < $arguments.length; i++) {
            $.put($arguments[i], args[i]);
        }
        $.lineDispatcher();
        return $.get("$");
    }

    public void dataFixer() {
        for (int i = 0; i < $arguments.length; i++) {
            $arguments[i] = $arguments[i].trim();
        }
        for (int i = 0; i < $lines.size(); i++) {
            String line = $lines.get(i);
            if (line.startsWith("*import") || line.startsWith("*reimport")) {
                CogModule.dataFix.warn("Line {} was removed from property due to being in property", line);
                $lines.remove(i); i--; continue;
            }
        }
    }

    @Override
    public String toString() {
        return "ModuleProperty{" +
                "$lines=[" + String.join("<|>", $lines) +
                "], $parent=" + $parent +
                ", $arguments=" + Arrays.toString($arguments) +
                '}';
    }
}
