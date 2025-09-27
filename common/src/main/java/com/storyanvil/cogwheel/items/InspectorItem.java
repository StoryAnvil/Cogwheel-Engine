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

package com.storyanvil.cogwheel.items;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.network.devui.inspector.InspectableBlock;
import com.storyanvil.cogwheel.network.devui.inspector.InspectableEntity;
import com.storyanvil.cogwheel.network.mc.Notification;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

public class InspectorItem extends Item {
    public InspectorItem(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if (!ctx.getWorld().isClient) {
            BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos());
            ServerWorld level = (ServerWorld) ctx.getWorld();
            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getPlayer();
            if (state.getBlock() instanceof InspectableBlock ib && devEnvCheck(player)) {
                if (player == null) return super.useOnBlock(ctx);
                if (ib.tryToInspect(level, player, state, ctx)) {
                    return ActionResult.CONSUME;
                }
            }
        }
        return super.useOnBlock(ctx);
    }

    @Override
    public @NotNull ActionResult useOnEntity(@NotNull ItemStack stack, @NotNull PlayerEntity _player, @NotNull LivingEntity target, @NotNull Hand hand) {
        if (_player instanceof ServerPlayerEntity player && stack.isOf(CogwheelHooks.getInspectorItem()) && target instanceof InspectableEntity ie && devEnvCheck(player)) {
            if (ie.tryToInspect(player.getServerWorld(), player)) {
                return ActionResult.CONSUME;
            }
        }
        return super.useOnEntity(stack, _player, target, hand);
    }

    private static boolean devEnvCheck(ServerPlayerEntity plr) {
        if (CogwheelConfig.isDevEnvironment()) return true;
        else {
            CogwheelHooks.sendPacket(new Notification(
                    Text.translatable("ui.storyanvil_cogwheel.notif_ban"), Text.translatable("ui.storyanvil_cogwheel.notif_ban_msg")
            ), plr);
            return false;
        }
    }
}
