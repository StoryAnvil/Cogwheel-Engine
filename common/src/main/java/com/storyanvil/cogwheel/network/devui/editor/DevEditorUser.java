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

package com.storyanvil.cogwheel.network.devui.editor;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.network.devui.DevEditorState;
import com.storyanvil.cogwheel.network.devui.DevEditorUserDelta;
import com.storyanvil.cogwheel.network.devui.CogwheelNetwork;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class DevEditorUser {
    private DevEditorSession session;
    private WeakReference<ServerPlayerEntity> player;
    private int line = 0;
    private int pos = 0;
    private int selectedChars = 0;
    private int color = 0;

    public DevEditorUser(DevEditorSession session, @NotNull ServerPlayerEntity player) {
        if (player == null) throw new IllegalStateException("Player is null");
        this.session = session;
        this.player = new WeakReference<>(player);
    }

    public boolean isInvalid() {
        return player.refersTo(null);
    }

    @SuppressWarnings("UnusedAssignment")
    public void dispose() {
        ServerPlayerEntity plr = player.get();
        if (plr != null) {
            CogwheelHooks.sendPacket(plr, new DevEditorState(session.lc, (byte) -128));
            plr = null;
        }
        session = null;
        player.clear();
        player = null;
    }

    public boolean refersTo(ServerPlayerEntity plr) {
        return player.refersTo(plr);
    }

    public ServerPlayerEntity get() {
        return player.get();
    }

    public DevEditorUserDelta toDelta() {
        return new DevEditorUserDelta(session.lc, line, pos, selectedChars, player.get().getNameForScoreboard(), color);
    }
    public void applyDelta(DevEditorUserDelta delta) {
        this.pos = delta.pos();
        this.line = delta.line();
        this.selectedChars = delta.selected();
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        if (line < 0) {this.line = 0; return;}
        this.line = line;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        if (pos <= 0) {this.pos = 0; return;}
        this.pos = pos;
    }

    public int getSelectedChars() {
        return selectedChars;
    }

    public void setSelectedChars(int selectedChars) {
        this.selectedChars = selectedChars;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
