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
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.props.ImplJWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Class;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import io.github.classgraph.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

@JWCGPM_Class(includeInDocs = false)
public class DocsGenerator {
    private static void sendMessage(Text msg) {
        CogwheelEngine.LOGGER.info("DocsGenerator: {}", msg.getString());
        try {
            MutableText append = Text.literal("[CogwheelEngine]").formatted(Formatting.GRAY)
                    .append(Text.literal(" ").formatted(Formatting.RESET, Formatting.WHITE))
                    .append(msg);
            for (ServerPlayerEntity plr : CogwheelHooks.getServerSideOverworld().getServer().getPlayerManager().getPlayerList()) {
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
            HashSet<String> validFiles = new HashSet<>();
            sendMessage(Text.literal("JWCGPMs collected!"));
            File docsDirectory = new File(CogwheelClientConfig.getDocsDirectory());
            if (!docsDirectory.exists()) {
                if (!docsDirectory.mkdirs()) {
                    sendMessage(Text.literal("Failed to create docs folder!").formatted(Formatting.RED));
                    return;
                }
            }
            ClassInfoList jwcgpms = scanResult.getSubclasses(ImplJWCGPM.class).union(
                    scanResult.getClassesWithAnnotation(JWCGPM_Class.class),
                    scanResult.getSubclasses(JWCGPM.class)
            );
            HashMap<String, JWCGPM_Class> classData = new HashMap<>();
            for (ClassInfo classInfo : jwcgpms) {
                if (classInfo.hasAnnotation(JWCGPM_Class.class)) {
                    AnnotationInfo annotationInfo = classInfo.getAnnotationInfo(JWCGPM_Class.class);
                    JWCGPM_Class jwcgpmClass = (JWCGPM_Class) annotationInfo.loadClassAndInstantiate();
                    classData.put(classInfo.getName(), jwcgpmClass);
                }
            }
            JWCGPM_Class jwcgpmClassDefault = new JWCGPM_Class(){
                @Override
                public Class<? extends Annotation> annotationType() {
                    return JWCGPM_Class.class;
                }

                @Override
                public boolean includeInDocs() {
                    return true;
                }
            };
            for (ClassInfo classInfo : jwcgpms) {
                JWCGPM_Class jwcgpmClass = classData.getOrDefault(classInfo.getName(), jwcgpmClassDefault);
                if (!jwcgpmClass.includeInDocs()) {
                    sendMessage(Text.literal("Skipped " + classInfo.getName()));
                    continue;
                }
                File jsonFile = new File(docsDirectory, classInfo.getName().replace('$', '_') + ".diff.json");
                sendMessage(Text.literal("Documenting " + classInfo.getName()));
                DiffJson docRoot = DiffJson.readFromFile(jsonFile);

                JsonArray array = new JsonArray(1);
                for (ClassInfo superClass : classInfo.getSuperclasses().union(classInfo.getInterfaces())) {
                    if (jwcgpms.contains(superClass)) {
                        JWCGPM_Class data = classData.getOrDefault(superClass.getName(), jwcgpmClassDefault);
                        if (data.includeInDocs())
                            array.add(superClass.getName());
                    }
                }
                docRoot.checkValue("parents", array);

                validFiles.add(jsonFile.getName());
                docRoot.checkValueString("type", "OBJECT");
                docRoot.checkString("summary", "... short description of this class ...");
                DiffJson docMethods = docRoot.checkDiffJsonObj("methods", new JsonObject());
                HashSet<String> methodsFound = new HashSet<>();
                for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()) {
                    if (methodInfo.hasAnnotation(JWCGPM_Method.class)) {
                        JWCGPM_Method jwcgpmMethod = (JWCGPM_Method) methodInfo.getAnnotationInfo(JWCGPM_Method.class).loadClassAndInstantiate();
                        methodsFound.add(methodInfo.getName());
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
                            } else if (classRefReturn.getFullyQualifiedClassName().equals("com.storyanvil.cogwheel.infrastructure.props.CGPM")) {
                                docMethod.checkString("return", "any");
                            } else {
                                docMethod.checkString("return", classRefReturn.getFullyQualifiedClassName());
                            }
                        } else {
                            docMethod.checkString("return", "...unknown return type...");
                        }
                    }
                }
                ArrayList<String> keys = new ArrayList<>();
                for (String s : docMethods.keys())
                    keys.add(s);
                for (String docMethod : keys) {
                    if (!methodsFound.contains(docMethod)) {
                        docMethods.checkUnset(docMethod);
                    }
                }
                try {
                    if (!docRoot.isStillValid())
                        invalidFiles.add(jsonFile.getName() + " [was auto edited]");
                    docRoot.writeToFile(jsonFile);
                } catch (IOException e) {
                    CogwheelEngine.LOGGER.error("Failed to write doc file {}", jsonFile, e);
                }
            }
            File[] allPages = docsDirectory.listFiles();
            if (allPages == null) allPages = new File[0];
            for (File doc : allPages) {
                String docName = doc.getName();
                if (!validFiles.contains(docName)) {
                    invalidFiles.add(docName + " [should not exist]");
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
