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

package com.storyanvil.cogwheel.network.belt;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventType;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.cog.CogString;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class Handlers {
    public static void none(BeltPacket packet) {

    }

    public static void cmd(BeltPacket packet) {
        CogwheelExecutor.scheduleTickEvent(tick -> {
            tick.level.getServer().getCommands().performPrefixedCommand(
                    new CommandSourceStack(CommandSource.NULL, new Vec3(0, 0, 0),
                            new Vec2(0, 0), (ServerLevel) tick.level, 4, "COGWHEEL", Component.literal("COGWHEEL"), tick.level.getServer(), null),
                    packet.getData()[0]);
        });
    }

    public static void dispatchScript(BeltPacket packet) {
        CogScriptDispatcher.dispatch(packet.getData()[0]);
    }

    public static void scriptMessage(BeltPacket packet) {
        HashMap<String, CogPropertyManager> storage = new HashMap<>();
        storage.put("msg", new CogString(packet.getData()[0]));
        EventType.dispatchEvent(EventType.BELT_MESSAGE, storage);
    }
}
