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

package com.storyanvil.cogwheel.mixin;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.storyanvil.cogwheel.network.devui.DevNetwork;
import com.storyanvil.cogwheel.network.devui.DevOpenViewer;
import com.storyanvil.cogwheel.network.devui.inspector.InspectableBlock;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.io.IOException;
import java.io.StringWriter;

@Mixin(BaseEntityBlock.class)
public class BaseEntityBlockMixin implements InspectableBlock {
    @Override
    public boolean tryToInspect(@NotNull ServerLevel level, @NotNull ServerPlayer player, @NotNull BlockState state, @NotNull UseOnContext ctx) {
        BlockEntity entity = level.getBlockEntity(ctx.getClickedPos());
        if (entity == null) return false;
        JsonObject nbt = StoryUtils.toCompoundJSON(entity.serializeNBT());
        nbt.addProperty("__cogwheel_engine_class", this.getClass().getCanonicalName());

        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setLenient(true);
            jsonWriter.setIndent("    ");
            jsonWriter.setSerializeNulls(true);
            Streams.write(nbt, jsonWriter);
            DevNetwork.sendFromServer(player, new DevOpenViewer("block.json", stringWriter.toString()));
            return true;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
