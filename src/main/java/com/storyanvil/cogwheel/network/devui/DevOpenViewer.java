/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.network.devui;

import com.storyanvil.cogwheel.client.devui.DWCodeViewer;
import com.storyanvil.cogwheel.client.devui.DevUI;
import com.storyanvil.cogwheel.client.devui.DevUIScreen;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecBuilder;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DevOpenViewer(String name, String code) implements StoryPacket {
    public static final StoryCodec<DevOpenViewer> CODEC = StoryCodecBuilder.build(
            StoryCodecBuilder.String(DevOpenViewer::name),
            StoryCodecBuilder.String(DevOpenViewer::code),
            DevOpenViewer::new
    );

    @Override
    public void onClientUnsafe(Supplier<NetworkEvent.Context> ctx) {
        DevUI i = DevUI.getInstance();
        if (i == null) {clientError(ctx, "Open DevUI at least once before using inspector!");return;}
        DWCodeViewer viewer = new DWCodeViewer(name);
        viewer.setCode(code);
        i.addTab(viewer);
        Minecraft.getInstance().player.level().playLocalSound(
                Minecraft.getInstance().player.getX(),
                Minecraft.getInstance().player.getY(),
                Minecraft.getInstance().player.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.NEUTRAL,
                2,
                1,
                false
        );
        Minecraft.getInstance().setScreen(new DevUIScreen());
    }
}
