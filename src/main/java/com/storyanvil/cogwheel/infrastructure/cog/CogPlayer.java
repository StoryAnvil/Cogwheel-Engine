/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.data.CameraPos;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.network.mc.CameraForceBound;
import com.storyanvil.cogwheel.network.mc.CameraTransitionBound;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.DialogResponseBound;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CogPlayer extends CogEntity implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("player", CogPlayer::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("sendMessage", (name, args, script, o) -> {
            CogPlayer p = (CogPlayer) o;
            if (p.player.refersTo(null)) throw new RuntimeException("Player got unloaded");
            p.player.get().sendSystemMessage(Component.literal(args.getString(0)));
            return null;
        });
        manager.reg("toEntity", (name, args, script, o) -> {
            return new CogEntity(((CogPlayer) o).player.get());
        });
        manager.reg("takeItem", (name, args, script, o) -> {
            CogPlayer pll = (CogPlayer) o;
            ServerPlayer p = Objects.requireNonNull(pll.player.get(), "player got unloaded");
            p.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
            return null;
        });
        manager.reg("getStoryTag", (name, args, script, o) -> {
            CogPlayer pll = (CogPlayer) o;
            ServerPlayer p = Objects.requireNonNull(pll.player.get(), "player got unloaded");
            CompoundTag shareTag = p.getItemInHand(InteractionHand.MAIN_HAND).getShareTag();
            if (shareTag == null) return null;
            String s = shareTag.getString("story");
            return new CogString(s);
        });
        manager.reg("setCamera", (name, args, script, o) -> {
            CogPlayer pll = (CogPlayer) o;
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(pll.player::get), new CameraForceBound(
                    new CameraPos(new Vec3(args.requireDouble(0), args.requireDouble(1), args.requireDouble(2)), (float) args.requireDouble(3), (float) args.requireDouble(4))
            ));
            return null;
        });
        manager.reg("unsetCamera", (name, args, script, o) -> {
            CogPlayer pll = (CogPlayer) o;
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(pll.player::get), new CameraForceBound(null));
            return null;
        });
        manager.reg("transCamera", (name, args, script, o) -> {
            CogPlayer pll = (CogPlayer) o;
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(pll.player::get), new CameraTransitionBound(
                    new CameraPos(new Vec3(args.requireDouble(0), args.requireDouble(1), args.requireDouble(2)), (float) args.requireDouble(3), (float) args.requireDouble(4)),
                    new CameraPos(new Vec3(args.requireDouble(5), args.requireDouble(6), args.requireDouble(7)), (float) args.requireDouble(8), (float) args.requireDouble(9)),
                    (float) args.requireDouble(10)
            ));
            return null;
        });
        manager.reg("transCameraEnd", (name, args, script, o) -> {
            CogPlayer pll = (CogPlayer) o;
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(pll.player::get), new CameraTransitionBound(
                    null, null, 0
            ));
            return null;
        });
    }

    private WeakReference<ServerPlayer> player;

    @Api.Experimental(since = "2.0.0")
    public CogPlayer(@NotNull WeakReference<ServerPlayer> player) {
        super(player.get());
        this.player = player;
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogPlayer other) {
            return other.player.equals(this.player);
        }
        return false;
    }
}
