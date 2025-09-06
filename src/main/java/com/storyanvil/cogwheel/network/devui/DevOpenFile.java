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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecs;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.Supplier;

import static com.storyanvil.cogwheel.data.StoryCodecBuilder.*;

public record DevOpenFile(ResourceLocation script, String code) implements StoryPacket {
    public static final StoryCodec<DevOpenFile> CODEC = build(
            Prop(DevOpenFile::script, StoryCodecs.RESOURCE_LOC),
            String(DevOpenFile::code),
            DevOpenFile::new
    );

    @Override
    public void onServerUnsafe(Supplier<NetworkEvent.Context> ctx) {
        String code = "#\\no-devui\\";
        StringBuilder sb = new StringBuilder();
        File script = CogScriptEnvironment.getScriptFile(script());
        try (FileReader fr = new FileReader(script); Scanner sc = new Scanner(fr)) {
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine()).append('\n');
            }
            code = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DevNetwork.sendFromServer(ctx.get().getSender(), new DevOpenFile(script(), code));
    }

    @Override
    public void onClientUnsafe(Supplier<NetworkEvent.Context> ctx) {
        CogwheelExecutor.log.warn("DevOpenFile{script={}, code='{}'}", script, code);
    }
}
