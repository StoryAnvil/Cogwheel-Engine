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

package com.storyanvil.cogwheel.cog.obj;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import com.storyanvil.cogwheel.network.mc.AnimationDataBound;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.stream.Collectors;

public class CogMaster extends JWCGPM<CogMaster> {
    public CogMaster() {}
    private static final Random random = new Random();

    // ========== === [ JWCGPM METHODS START HERE ] === ========== \\
    @JWCGPM_Method(arguments = {CGPM.class})
    public @NotNull CogNullManager log(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        CogwheelExecutor.log.info("{}: {}", script.getScriptName(), data.get(0).convertToString());
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CGPM.class})
    public @NotNull CGPM debugLog(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        if (CogwheelConfig.isDevEnvironment())
            CogwheelExecutor.log.info("{}: [DGB] {}", script.getScriptName(), data.get(0).convertToString());
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CGPM isDevEnv(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(CogwheelConfig.isDevEnvironment());
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CGPM getOverworld(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return new CogWorld(CogwheelHooks.getServerSideOverworld());
    }
    @JWCGPM_Method(arguments = {CogIdentifier.class})
    public @NotNull CGPM getWorld(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        try {
            ServerWorld world = StoryUtils.getServerWorldFrom(CogwheelHooks.getMinecraftServer(), data.requireIdentifier(0));
            if (world == null) {
                return CogUtils.NULL;
            }
            return new CogWorld(world);
        } catch (Exception e) {
            throw script.wrap(e);
        }
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager clearVar(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        script.put(data.requireString(0), null);
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogInteger.class})
    public @NotNull CogNullManager sleep(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        throw new PreventChainCalling(new ChainCallPostPrevention() {
            @Override
            public void prevent(String variable) throws CogScriptException {
                CogwheelExecutor.schedule(script::lineDispatcher, data.requireInt(0));
                script.put(variable, CogUtils.NULL);
            }
        });
    }
    @JWCGPM_Method(arguments = {CogIdentifier.class})
    public @NotNull CogNullManager dispatchScriptGlobally(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        CogScriptEnvironment.dispatchScriptGlobal(data.requireIdentifier(0));
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager dispatchScriptLocally(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        script.getEnvironment().dispatchScript(data.requireString(0));
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager waitForStoryAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        throw new PreventChainCalling(new ChainCallPostPrevention() {
            @Override
            public void prevent(String variable) throws CogScriptException {
                CogwheelExecutor.getDefaultEnvironment().registerLabelListener(data.requireString(0), (s, storyAction) -> {
                    CogwheelExecutor.schedule(script::lineDispatcher);
                });
            }
        });
    }
    @JWCGPM_Method(arguments = {CogIdentifier.class})
    public @NotNull CogNullManager addAnimationSource(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        Identifier loc = data.requireIdentifier(0).withSuffixedPath(".json");
        CogwheelExecutor.getDefaultEnvironment().serverSideAnimations.add(loc);
        CogwheelHooks.sendPacketToEveryone(new AnimationDataBound(
                CogwheelExecutor.getDefaultEnvironment().serverSideAnimations.stream().map(Identifier::toString).collect(Collectors.joining("|"))
        ));
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogIdentifier.class})
    public @NotNull CogNullManager removeAnimationSource(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        Identifier loc = data.requireIdentifier(0).withSuffixedPath(".json");
        CogwheelExecutor.getDefaultEnvironment().serverSideAnimations.remove(loc);
        CogwheelHooks.sendPacketToEveryone(new AnimationDataBound(
                CogwheelExecutor.getDefaultEnvironment().serverSideAnimations.stream().map(Identifier::toString).collect(Collectors.joining("|"))
        ));
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogNullManager clearAnimationSources(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        CogwheelExecutor.getDefaultEnvironment().serverSideAnimations.clear();
        CogwheelHooks.sendPacketToEveryone(new AnimationDataBound(""));
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogLong currentTimeMS(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogLong(System.currentTimeMillis());
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogNullManager dumpVariables(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        StringBuilder sb = new StringBuilder();
        script.dumpVariables(sb);
        for (String line : sb.toString().split("\n")) {
            CogwheelExecutor.log.info("{}: {}", script.getScriptName(), line);
        }
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogBool.class, CGPM.class, CGPM.class})
    public @NotNull CGPM choose(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return data.requireBoolean(0) ? data.get(1) : data.get(2);
    }
    @JWCGPM_Method(arguments = {CogInteger.class, CGPM.class})
    public @NotNull CGPM map(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        int b = data.requireInt(0);
        return data.get(b + 1);
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager executeCommand(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        CogwheelHooks.executeCommand(data.requireString(0));
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogInteger.class, CogInteger.class})
    public @NotNull CogInteger randomInt(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogInt(random.nextInt(data.requireInt(0), data.requireInt(1)));
    }
}
