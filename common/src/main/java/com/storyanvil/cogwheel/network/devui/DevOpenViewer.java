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

package com.storyanvil.cogwheel.network.devui;

import com.storyanvil.cogwheel.client.devui.DWCodeViewer;
import com.storyanvil.cogwheel.client.devui.DevUI;
import com.storyanvil.cogwheel.client.devui.DevUIScreen;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecBldr;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public record DevOpenViewer(String name, String code) implements StoryPacket<DevOpenViewer> {
    public static final StoryCodec<DevOpenViewer> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.String(DevOpenViewer::name),
            StoryCodecBldr.String(DevOpenViewer::code),
            DevOpenViewer::new
    );

    @Override
    public StoryCodec<DevOpenViewer> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        DevUI i = DevUI.getInstance();
        if (i == null) {clientError(ctx, "Open DevUI at least once before using inspector!");return;}
        DWCodeViewer viewer = new DWCodeViewer(name);
        viewer.setCode(code);
        i.addTab(viewer);
        MinecraftClient.getInstance().player.getWorld().playSound(
                MinecraftClient.getInstance().player.getX(),
                MinecraftClient.getInstance().player.getY(),
                MinecraftClient.getInstance().player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE,
                SoundCategory.NEUTRAL,
                2,
                1,
                false
        );
        MinecraftClient.getInstance().setScreen(new DevUIScreen());
    }
}
