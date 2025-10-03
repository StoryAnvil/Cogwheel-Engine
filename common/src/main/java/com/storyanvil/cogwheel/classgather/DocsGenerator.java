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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.config.CogwheelClientConfig;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.util.StoryUtils;
import io.github.classgraph.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class DocsGenerator {
    private static void sendMessage(Text msg) {
        CogwheelEngine.LOGGER.info("DocsGenerator: {}", msg.getString());
        try {
            MutableText append = Text.literal("[CogwheelEngine]").formatted(Formatting.GRAY)
                    .append(Text.literal(" ").formatted(Formatting.RESET, Formatting.WHITE))
                    .append(msg);
            for (ServerPlayerEntity plr : CogwheelHooks.getOverworldServer().getServer().getPlayerManager().getPlayerList()) {
                plr.sendMessage(append);
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void generateDocumentation() {
        CogwheelExecutor.schedule(DocsGenerator::run);
    }

    private static void run() {
        sendMessage(Text.literal("Collecting JWCGPMs"));
        try (ScanResult scanResult = new ClassGraph()
//                    .verbose()
//                    .enableRealtimeLogging()
                .enableAllInfo()
                .overrideClassLoaders(new URLClassLoader(
                        new URL[]{new File("/home/denisjava/IdeaProjects/Cogwheel-Engine/neoforge/build/libs/storyanvil_cogwheel-neoforge-3.0.0.jar").toURI().toURL()},
                        DocsGenerator.class.getClassLoader()
                ))
                .ignoreParentClassLoaders()
                .acceptPackages("com.storyanvil.cogwheel")
                .scan()) {
            ArrayList<String> invalidFiles = new ArrayList<>();
            sendMessage(Text.literal("JWCGPMs collected!"));
            File docsDirectory = new File(CogwheelClientConfig.getDocsDirectory());
            if (!docsDirectory.exists()) {
                if (!docsDirectory.mkdirs()) {
                    sendMessage(Text.literal("Failed to create docs folder!").formatted(Formatting.RED));
                    return;
                }
            }
            ClassInfoList jwcgpms = scanResult.getSubclasses(JWCGPM.class);
            for (ClassInfo classInfo : jwcgpms) {
                sendMessage(Text.literal("Documenting " + classInfo.getName()));
                Class<?> actualClass = Class.forName(classInfo.getName());
                File jsonFile = new File(docsDirectory, classInfo.getName().replace('$', '_') + ".diff.json");
                DiffJson docRoot = DiffJson.readFromFile(jsonFile);
                docRoot.checkValueString("type", "OBJECT");
                docRoot.checkString("summary", "... short description of this class ...");
                DiffJson docMethods = docRoot.checkDiffJsonObj("methods", new JsonObject());
                for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()) {
                    if (methodInfo.hasAnnotation(JWCGPM_Method.class)) {
                        JWCGPM_Method jwcgpmMethod = (JWCGPM_Method) methodInfo.getAnnotationInfo(JWCGPM_Method.class).loadClassAndInstantiate();
                        DiffJson docMethod = docMethods.checkDiffJsonObj(methodInfo.getName(), new JsonObject());
                        JsonArray argTypes = new JsonArray();
                        JsonArray argNames = new JsonArray();
                        int argN = 0;
                        for (Class<?> arg : jwcgpmMethod.arguments()) {
                            argTypes.add(arg.getCanonicalName());
                            argNames.add("arg" + argN);
                            argN++;
                        }
                        docMethod.checkArray("argTypes", argTypes);
                        docMethod.checkArray("argNames", argNames);
                        docMethod.checkString("summary", "... short description of this property ...");
                        docMethod.checkBoolean("preventsChainCalling", false);

                        // Check return
                        TypeSignature resultType = methodInfo.getTypeSignatureOrTypeDescriptor().getResultType();
                        if (resultType instanceof ClassRefTypeSignature classRefReturn) {
                            if (classRefReturn.getFullyQualifiedClassName().equals("com.storyanvil.cogwheel.cog.obj.CogNullManager")) {
                                docMethod.checkString("return", "null");
                            } else {
                                docMethod.checkString("return", classRefReturn.getFullyQualifiedClassName());
                            }
                        } else {
                            docMethod.checkString("return", "...unknown return type...");
                        }
                    }
                }
                try {
                    if (!docRoot.isStillValid())
                        invalidFiles.add(jsonFile.getCanonicalPath());
                    docRoot.writeToFile(jsonFile);
                } catch (IOException e) {
                    CogwheelEngine.LOGGER.error("Failed to write doc file {}", jsonFile, e);
                }
            }
            sendMessage(Text.literal("Docs first phase finished!"));
            try (FileWriter fw = new FileWriter(new File(docsDirectory, "checklist.md"))) {
                fw.append("# Changed files\n");
                fw.append("Following files were changed. Make sure to remove all `+` and `-` signs in json keys before publishing\n\n");
                for (String file : invalidFiles) {
                    fw.append("- [ ] ");
                    fw.append(file);
                    fw.append('\n');
                }
                fw.flush();
            }
            invalidFiles.clear();
            invalidFiles = null;
        } catch (Throwable e) {
            sendMessage(Text.literal("Docs gen failed!"));
            CogwheelEngine.LOGGER.error("Docs gen failed!", e);
            return;
        }
    }
}
