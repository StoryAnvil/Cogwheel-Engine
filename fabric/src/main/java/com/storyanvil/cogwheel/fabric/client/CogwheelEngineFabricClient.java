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

package com.storyanvil.cogwheel.fabric.client;

import com.storyanvil.cogwheel.fabric.FabricRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class CogwheelEngineFabricClient implements ClientModInitializer {

    public static KeyBinding devUI;
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FabricRegistry.NPC, NPCRenderer::new);
        devUI = KeyBindingHelper.registerKeyBinding(new KeyBinding("ui.storyanvil_cogwheel.dev_ui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "ui.storyanvil_cogwheel"));
    }
}
