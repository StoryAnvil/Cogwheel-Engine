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

package com.storyanvil.cogwheel.network.devui.editor;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.data.SyncArray;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.network.devui.*;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.Notification;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DevEditorSession {
    private static final HashMap<ResourceLocation, DevEditorSession> sessions = new HashMap<>();

    private static final HashMap<String, Integer> cursorColors = new HashMap<>();
    private static final AtomicInteger currentColor = new AtomicInteger(0);
    private static final int[] COLORS = new int[]{
            -922812416, -922790912, -922748160, -932643072, -939458643, -939495199, -935890719, -923664159
    };

    public static synchronized void boundColorFor(ServerPlayer plr) {
        cursorColors.put(plr.getScoreboardName(), COLORS[currentColor.get()]);
        if (currentColor.addAndGet(1) >= COLORS.length) {
            currentColor.set(0);
        }
    }
    public static synchronized void unboundColorFrom(ServerPlayer plr) {
        cursorColors.remove(plr.getScoreboardName());
    }
    public static int getColorFor(ServerPlayer plr) {
        return cursorColors.get(plr.getScoreboardName());
    }



    public static DevEditorSession createOrGet(ResourceLocation lc) {
        if (sessions.containsKey(lc)) {
            return sessions.get(lc);
        }
        DevEditorSession session = new DevEditorSession(lc);
        sessions.put(lc, session);
        return session;
    }
    public static DevEditorSession get(ResourceLocation lc) {
        return sessions.get(lc);
    }

    public static Collection<DevEditorSession> getSessions() {
        return sessions.values();
    }

    private CogScriptEnvironment env;
    protected ResourceLocation lc;
    private File file;
    protected SyncArray<String> lines = new SyncArray<>();
    protected ArrayList<DevEditorUser> connections = new ArrayList<>();
    private DevEditorSession(ResourceLocation lc) {
        this.lc = lc;
        this.file = CogScriptEnvironment.getScriptFile(lc);
        this.env = CogScriptEnvironment.getEnvironment(lc);
        log.info("Created EditorSession for {}({})", file, lc);
    }

    public synchronized void resyncAll() {
        for (int i = 0; i < connections.size(); i++) {
            DevEditorUser con = connections.get(i);
            if (con.isInvalid()) {
                i--;
                connections.remove(i);
                con.dispose();
                continue;
            }
            resync(con);
        }
    }
    public synchronized void resync(DevEditorUser con) {
        for (int i = 0; i < lines.size(); i++) {
            DevNetwork.sendFromServer(con.get(), new DevEditorLine(lc, i, lines.get(i), lines.size()));
        }
        for (int i = 0; i < connections.size(); i++) {
            DevEditorUser con2 = connections.get(i);
            if (con2.isInvalid()) {
                i--;
                connections.remove(i);
                continue;
            }
            DevNetwork.sendFromServer(con.get(), con2.toDelta());
        }
    }

    public synchronized void read() throws IOException {
        if (!env.canBeEdited())
            throw new IOException("Scripts in environment \"" + env.toString() + "\" does not allow script editing");
        if (!file.exists()) {
            lines.clear();
            lines.add("");
            resyncAll();
            return;
        }
        try (FileReader fr = new FileReader(file); Scanner sc = new Scanner(fr)) {
            lines.clear();
            while (sc.hasNextLine())
                lines.add(sc.nextLine());
            resyncAll();
        }
    }

    public synchronized void dispose() {
        for (DevEditorUser con : connections) {
            con.dispose();
        }
        connections.clear();
        connections = null;
        lines.clear();
        lines = null;
        sessions.remove(lc);
        log.info("Disposed EditorSession for {}({})", file, lc);
        lc = null;
        file = null;
    }

    public synchronized void addConnection(ServerPlayer plr) {
        DevEditorUser con = new DevEditorUser(this, plr);
        connections.add(con);
        DevNetwork.sendFromServer(plr, new DevOpenFile(lc));
        con.setColor(getColorFor(plr));
    }

    public synchronized void closeConnection(NetworkEvent.Context ctx) {
        DevEditorUser con = getConnection(ctx.getSender());
        if (con == null) return;
        connections.remove(con);
        con.dispose();
        resyncAll();
        if (connections.isEmpty()) {
            dispose();
        }
    }

    private synchronized DevEditorUser getConnection(ServerPlayer plr) {
        for (int i = 0; i < connections.size(); i++) {
            DevEditorUser con = connections.get(i);
            if (con.refersTo(plr))
                return con;
            if (con.isInvalid()) {
                connections.remove(i);
                i--;
                con.dispose();
            }
        }
        return null;
    }

    public synchronized void resync(@Nullable ServerPlayer sender) {
        if (sender == null) return;
        DevEditorUser con = getConnection(sender);
        if (con == null) return;
        resync(con);
    }

    public synchronized void updateConnection(@Nullable ServerPlayer sender, DevEditorUserDelta delta) {
        DevEditorUser user = getConnection(sender);
        if (user == null) {
            if (sender != null)
                DevNetwork.sendFromServer(sender, new DevEditorState(lc, (byte) -128));
            return;
        }
        user.setLine(delta.line());
        user.setPos(delta.line());
        user.setSelectedChars(delta.selected());

        // User could tamper with name
        DevEditorUserDelta safeDelta = user.toDelta();
        for (int i = 0; i < connections.size(); i++) {
            DevEditorUser con = connections.get(i);
            if (con == user) return;
            if (con.isInvalid()) {
                i--;
                connections.remove(i);
                con.dispose();
                continue;
            }
            DevNetwork.sendFromServer(con.get(), safeDelta);
        }
    }

    public synchronized void typeCallback(@Nullable ServerPlayer sender, DevTypeCallback callback) {
        DevEditorUser user = getConnection(sender);
        if (user == null) {
            if (sender != null)
                DevNetwork.sendFromServer(sender, new DevEditorState(lc, (byte) -128));
            return;
        }
        user.applyDelta(callback.delta());

        if (user.getPos() <= 0 && callback.typed().equals("<backspace>")) {
            if (user.getLine() <= 0) return;
            user.setPos(0);
            DevDeleteLine delta1 = new DevDeleteLine(lc, user.getLine());
            int last = user.getLine() - 1;
            String line = lines.get(last) + lines.get(user.getLine());
            DevEditorLine delta2 = new DevEditorLine(lc, last, line, lines.size() - 1);
            user.setLine(user.getLine() - 1);
            user.setPos(line.length() + 1);
            DevEditorUserDelta delta = user.toDelta();
            lines.remove(user.getLine());
            lines.set(last, line);
            for (int i = 0; i < connections.size(); i++) {
                DevEditorUser con = connections.get(i);
                if (con.isInvalid()) {
                    i--;
                    connections.remove(i);
                    continue;
                }
                ServerPlayer plr = con.get();
                DevNetwork.sendFromServer(plr, delta);
                DevNetwork.sendFromServer(plr, delta1);
                DevNetwork.sendFromServer(plr, delta2);
            }
            return;
        }

        StringBuilder diff = new StringBuilder(lines.get(user.getLine()));
        if (callback.typed().equals("<backspace>")) {
            if (user.getPos() <= 0) return;
            diff.deleteCharAt(user.getPos() - 1);
            user.setPos(user.getPos() - 1);
        } else if (callback.typed().equals("<delete>")) {
            if (user.getPos() < 0) return;
            diff.deleteCharAt(user.getPos());
        } else {
            if (diff.isEmpty()) {
                diff.append(callback.typed());
                user.setPos(user.getPos() + callback.typed().length() + 1);
            } else {
                diff.insert(user.getPos(), callback.typed());
                user.setPos(user.getPos() + 1);
            }
        }
        lines.set(user.getLine(), diff.toString());
        DevEditorUserDelta safeDelta = user.toDelta();
        DevEditorLine lineDelta = new DevEditorLine(lc, user.getLine(), diff.toString(), lines.size());
        for (int i = 0; i < connections.size(); i++) {
            DevEditorUser con = connections.get(i);
            if (con.isInvalid()) {
                i--;
                connections.remove(i);
                continue;
            }
            ServerPlayer plr = con.get();
            DevNetwork.sendFromServer(plr, lineDelta);
            DevNetwork.sendFromServer(plr, safeDelta);
        }
    }

    public synchronized void flush(@Nullable ServerPlayer sender) {

        try {
            if (!file.exists()) file.createNewFile();
            try (FileWriter fw = new FileWriter(file)) {
                for (String line : lines) {
                    fw.append(line).append('\n');
                }
                fw.flush();
                for (int i = 0; i < connections.size(); i++) {
                    DevEditorUser con = connections.get(i);
                    if (con.isInvalid()) {
                        i--;
                        connections.remove(i);
                        continue;
                    }
                    ServerPlayer plr = con.get();
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> plr), new Notification(
                            Component.literal("File saved"), Component.literal("File " + lc + " was saved!")
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Exception while flushing {}: {}", file, e);
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> sender), new Notification(
                    Component.literal("Save failed"), Component.literal("File " + lc + " was not saved!")
            ));
        }
    }

    public void dispatch() {
        CogwheelExecutor.schedule(() -> {
            CogScriptEnvironment.dispatchScriptGlobal(lc);
        });
    }

    public synchronized void flushAndDispatch(@Nullable ServerPlayer sender) {
        try (FileWriter fw = new FileWriter(file)) {
            for (String line : lines) {
                fw.append(line).append('\n');
            }
            fw.flush();
            for (int i = 0; i < connections.size(); i++) {
                DevEditorUser con = connections.get(i);
                if (con.isInvalid()) {
                    i--;
                    connections.remove(i);
                    continue;
                }
                ServerPlayer plr = con.get();
                dispatch();
                CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> plr), new Notification(
                        Component.literal("File saved and executed!"), Component.literal("File " + lc + " was saved!")
                ));
            }
        } catch (Exception e) {
            log.error("Exception while flushing {}: {}", file, e);
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> sender), new Notification(
                    Component.literal("Save failed"), Component.literal("File " + lc + " was not saved!")
            ));
        }
    }

    public synchronized void typeCallback(@Nullable ServerPlayer sender, DevEnterCallback callback) {
        if (callback.typed().equals("enter")) {
            DevEditorUser user = getConnection(sender);
            if (user == null) {
                if (sender != null)
                    DevNetwork.sendFromServer(sender, new DevEditorState(lc, (byte) -128));
                return;
            }
            DevInsertLine delta1;
            DevEditorLine delta2 = null;
            user.applyDelta(callback.delta());
            String line = lines.get(user.getLine());
            if (user.getPos() == line.length() + 1) {
                delta1 = new DevInsertLine(lc, user.getLine(), "");
            } else {
                delta1 = new DevInsertLine(lc, user.getLine(), line.substring(user.getPos()));
                delta2 = new DevEditorLine(lc, user.getLine(), line.substring(0, user.getPos()), lines.size() + 1);
            }
            lines.add(delta1.lineBefore() + 1, delta1.contents());
            if (delta2 != null)
                lines.set(delta2.lineNumber(), delta2.line());
            user.setLine(user.getLine() + 1);
            user.setPos(0);
            DevEditorUserDelta delta3 = user.toDelta();
            for (int i = 0; i < connections.size(); i++) {
                DevEditorUser con = connections.get(i);
                if (con.isInvalid()) {
                    i--;
                    connections.remove(i);
                    continue;
                }
                ServerPlayer plr = con.get();
                DevNetwork.sendFromServer(plr, delta1);
                DevNetwork.sendFromServer(plr, delta2);
                DevNetwork.sendFromServer(plr, delta3);
            }
        }
    }
}
