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

package com.storyanvil.cogwheel.neoforge;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.neoforge.data.NbtAttachment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class NeoRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CogwheelEngine.MODID);

    public static final Supplier<AttachmentType<NbtAttachment>> DATA = ATTACHMENT_TYPES.register(
            "data", () -> AttachmentType.serializable(() -> new NbtAttachment()).build()
    );
    public static Lazy<KeyBinding> OPEN_DEVUI = Lazy.of(() -> new KeyBinding("ui.storyanvil_cogwheel.dev_ui", KeyConflictContext.UNIVERSAL, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "ui.storyanvil_cogwheel"));
}
