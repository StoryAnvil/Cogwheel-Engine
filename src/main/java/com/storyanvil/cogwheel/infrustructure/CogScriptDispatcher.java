/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.CogwheelExecutor;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CogScriptDispatcher {
    public static void dispatch(String scriptName) {
        CogwheelExecutor.schedule(l -> dispatcher(scriptName, l));
    }
    private static void dispatcher(String scriptName, Logger log) {
        if (!scriptName.endsWith(".sa")) {
            log.error("Script {} does not end with .sa extension. Dispatch ignored!", scriptName);
            return;
        }
        File script = new File(Minecraft.getInstance().gameDirectory, "config/cog/" + scriptName);
        if (!script.exists()) {
            log.error("Script {} does not exist with .sa extension. Dispatch ignored!", script);
            return;
        }
        log.info("Script: {} dispatched", scriptName);
        try (FileReader fr = new FileReader(script); Scanner sc = new Scanner(fr);) {
            ArrayList<String> lines = new ArrayList<>();
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
            CogwheelExecutor.schedule(new DispatchedScript(lines).setScriptName(scriptName)::lineDispatcher);
        } catch (IOException e) {
            log.error("Script dispatch failed while file reading", e);
        }
    }
}
