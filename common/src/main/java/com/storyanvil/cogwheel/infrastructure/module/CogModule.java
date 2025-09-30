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

package com.storyanvil.cogwheel.infrastructure.module;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.cog.CogInvoker;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.err.CogExpressionFailure;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CogModule implements CGPM {
    public static CogModule build(CogScriptEnvironment env, String script) throws CogExpressionFailure {
        File scriptFile = new File(CogwheelHooks.getConfigFolder(), env.getScript(script));
        if (!scriptFile.exists()) {
            dataFix.error("Module {} does not exist!", script);
            throw new CogExpressionFailure("Module File does not exist!");
        }
        try (FileReader fr = new FileReader(scriptFile); Scanner sc = new Scanner(fr)) {
            CogModule module = new CogModule(env, script.substring(0, script.length() - 4));

            ArrayList<String> property = null;
            String propertyName = null;
            String[] propertyArgs = null;
            int level = 0;

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                if (property == null) {
                    if (line.startsWith("define ")) {
                        property = new ArrayList<>();
                        String header = line.substring(7);
                        int space = header.indexOf('(');
                        if (space == -1) throw new CogExpressionFailure("Module line \"" + line + "\" is invalid property header!");
                        propertyName = header.substring(0, space);
                        if (space == 0) {
                            propertyName = "<init>";
                            property.add("$ = this");
                        }
                        if (!header.endsWith(") {")) throw new CogExpressionFailure("Module line \"" + line + "\" is invalid property header [TAIL BRACKET MISMATCH]!");
                        String brackets = header.substring(space + 1, header.length() - 3);
                        propertyArgs = brackets.split(",");
                        level = 0;
                    }
                } else {
                    if (line.equals("}")) {
                        level--;
                        if (level < 0) {
                            ModuleProperty moduleProperty = new ModuleProperty(property, module, propertyArgs);
                            module.properties.put(propertyName, moduleProperty);
                            property = null;
                            propertyName = null;
                            propertyArgs = null;
                            level = 0;
                        }
                    } else if (line.endsWith("{")) {
                        level++;
                    } else {
                        property.add(line.trim());
                    }
                }
            }
            module.dataFixer();
            return module;
        } catch (IOException e) {
            throw new CogExpressionFailure("Module import failed while file reading", e);
        }
    }

    public CogModule(CogScriptEnvironment environment, String name) {
        this.properties = new HashMap<>();
        this.environment = environment;
        this.name = name;
    }

    private final HashMap<String, ModuleProperty> properties;
    private final CogScriptEnvironment environment;
    private final String name;

    public boolean _hasOwnProperty(String name) {
        if (name.startsWith(":")) {
            name = name.substring(1);
        }
        return properties.containsKey(name);
    }

    public Identifier getID() {
        return Identifier.of(environment.getUniqueIdentifier(), name);
    }

    public CGPM _getProperty(String name, ArgumentData args, DispatchedScript script, CMA instance) throws CogScriptException {
        if (name.startsWith(":")) {
            return CogInvoker.cmaInvoker(instance, name);
        }
        return properties.get(name).handle(name, args, script, instance);
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return name.equals("new") || name.equals("dump");
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling, CogScriptException {
        if (name.equals("dump")) {
            dataFix.info("MODULE DUMP: >{}<", this.getID());
            dataFix.info("METHODS:");
            for (Map.Entry<String, ModuleProperty> property : properties.entrySet()) {
                dataFix.info(" {} = {}", property.getKey(), property.getValue());
            }
            return null;
        }
        return properties.get("<init>").handle(name, args, script, null);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o == this;
    }

    public CogScriptEnvironment getEnvironment() {
        return environment;
    }

    public static final Logger dataFix = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/DATAFIXER");
    public void dataFixer() {
        for (ModuleProperty property : properties.values()) {
            property.dataFixer();
        }
    }

    @Override
    public String toString() {
        return "CogModule{" +
                "properties=" + properties +
                ", environment=" + environment +
                ", name='" + name + '\'' +
                '}';
    }
}
