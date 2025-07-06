package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.util.ActionFactory;
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

    /**
     * Parses StoryActionData to StoryAction
     */
    public static StoryAction<?> parseSAD(String data) {
        String[] dat = data.split(" ");
        ActionFactory factory = StoryAction.get(dat[0]);
        if (factory == null) throw new RuntimeException("No StoryAction named " + dat[0]);
        return factory.construct(dat);
    }
}
