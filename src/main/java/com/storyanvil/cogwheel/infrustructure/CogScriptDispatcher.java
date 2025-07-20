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

package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventType;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogScriptDispatcher {
    public static void dispatch(String scriptName) {
        CogwheelExecutor.schedule(() -> dispatcher(scriptName, new HashMap<>()));
    }
    public static void dispatch(String scriptName, HashMap<String, CogPropertyManager> storage) {
        CogwheelExecutor.schedule(() -> dispatcher(scriptName, storage));
    }
    private static void dispatcher(String scriptName, HashMap<String, CogPropertyManager> storage) {
        File script = new File(Minecraft.getInstance().gameDirectory, "config/cog/" + scriptName);
        if (!script.exists()) {
            log.error("Script {} does not exist. Dispatch ignored!", script);
            return;
        }
        if (scriptName.endsWith(".sa") /* storyanvil */) {
            log.info("Script: {} dispatched", scriptName);
            try (FileReader fr = new FileReader(script); Scanner sc = new Scanner(fr)) {
                ArrayList<String> lines = new ArrayList<>();
                while (sc.hasNextLine()) {
                    lines.add(sc.nextLine());
                }
                CogwheelExecutor.schedule(new DispatchedScript(lines, storage).setScriptName(scriptName)::lineDispatcher);
            } catch (IOException e) {
                log.error("Script dispatch failed while file reading", e);
            }
        } else if (scriptName.endsWith(".sam" /* storyanvil manifest */)) {
            try (FileReader fr = new FileReader(script); Scanner sc = new Scanner(fr)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] parts = line.split(" -> ");
                    if (parts.length != 2) log.error("Manifest line {} is not valid and will be ignored", line);
                    EventType type;
                    try {
                        type = EventType.valueOf(parts[0].trim());
                    } catch (IllegalArgumentException e) {log.error("Event type: \"{}\" was not recognized", parts[0].trim()); continue;}
                    EventType.setSubscriber(type, parts[1].trim());
                }
            } catch (IOException e) {
                log.error("Script dispatch failed while file reading", e);
            }
        } else {
            log.error("Script {} does not end with any known extension (known extensions are: \".sa\", \".sam\" ). Dispatch ignored!", script);
        }
    }
}
