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

package com.storyanvil.cogwheel.items;

import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.network.devui.inspector.InspectableBlock;
import com.storyanvil.cogwheel.network.devui.inspector.InspectableEntity;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.Notification;
import com.storyanvil.cogwheel.registry.CogwheelItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class InspectorItem extends Item {
    public InspectorItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide) {
            BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
            ServerLevel level = (ServerLevel) ctx.getLevel();
            ServerPlayer player = (ServerPlayer) ctx.getPlayer();
            if (state.getBlock() instanceof InspectableBlock ib && devEnvCheck(player)) {
                if (player == null) return super.useOn(ctx);
                if (ib.tryToInspect(level, player, state, ctx)) {
                    player.getCooldowns().addCooldown(CogwheelItems.INSPECTOR.get(), 10);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.useOn(ctx);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player _player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (_player instanceof ServerPlayer player && stack.is(CogwheelItems.INSPECTOR.get()) && target instanceof InspectableEntity ie && devEnvCheck(player)) {
            if (ie.tryToInspect(player.serverLevel(), player)) {
                player.getCooldowns().addCooldown(CogwheelItems.INSPECTOR.get(), 10);
                return InteractionResult.CONSUME;
            }
        }
        return super.interactLivingEntity(stack, _player, target, hand);
    }

    private static boolean devEnvCheck(ServerPlayer plr) {
        if (CogwheelConfig.isDevEnvironment()) return true;
        else {
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> plr), new Notification(
                    Component.translatable("ui.storyanvil_cogwheel.notif_ban"), Component.translatable("ui.storyanvil_cogwheel.notif_ban_msg")
            ));
            return false;
        }
    }
}
