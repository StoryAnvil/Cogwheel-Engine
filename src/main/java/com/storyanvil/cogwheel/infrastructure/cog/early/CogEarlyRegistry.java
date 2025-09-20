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

package com.storyanvil.cogwheel.infrastructure.cog.early;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.data.SyncArray;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CogEarlyRegistry implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("earlyreg", manager -> {
        manager.reg("item", (name, args, script, o) -> {
            CogEarlyRegistry registry = (CogEarlyRegistry) o;
            CogEarlyItem item = new CogEarlyItem(args.getString(0));
            registry.items.add(item);
            return item;
        });
    }, CogwheelEngine.EARLY_MANAGER);

    private final String namespace;

    private final DeferredRegister<Item> ITEMS;
    private final DeferredRegister<Block> BLOCKS;
    private final SyncArray<CogEarlyItem> items;
    private final SyncArray<CogEarlyBlock> blocks;

    public CogEarlyRegistry(String namespace) {
        this.namespace = namespace;
        this.ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, namespace);
        this.BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, namespace);
        CogwheelEngine.EARLY.info("Created registry with namespace {}", namespace);
        this.items = new SyncArray<>();
        this.blocks = new SyncArray<>();
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o instanceof CogEarlyRegistry;
    }

    public void register(IEventBus modEventBus) {
        items.freeze();
        blocks.freeze();
        for (CogEarlyItem earlyItem : items) {
            earlyItem.register(ITEMS);
        }
        for (CogEarlyBlock earlyItem : blocks) {
            earlyItem.register(BLOCKS);
        }
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CogwheelEngine.EARLY.info("Finished registry with namespace {}", namespace);
    }
}
