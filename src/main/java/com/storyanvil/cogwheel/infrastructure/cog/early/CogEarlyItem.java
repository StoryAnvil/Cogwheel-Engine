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
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public class CogEarlyItem implements CogPropertyManager{
    private static final EasyPropManager MANAGER = new EasyPropManager("earlyitem", manager -> {
        manager.reg("stacksTo", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setStacksTo(args.requireInt(0));
            return item;
        });
        manager.reg("durability", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setDurability(args.requireInt(0));
            return item;
        });
        manager.reg("commonRarity", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setRarity(Rarity.COMMON);
            return item;
        });
        manager.reg("uncommonRarity", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setRarity(Rarity.UNCOMMON);
            return item;
        });
        manager.reg("epicRarity", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setRarity(Rarity.EPIC);
            return item;
        });
        manager.reg("rareRarity", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setRarity(Rarity.RARE);
            return item;
        });
        manager.reg("noRepair", (name, args, script, o) -> {
            CogEarlyItem item = (CogEarlyItem) o;
            item.data.setNoRepair(true);
            return item;
        });
    }, CogwheelEngine.EARLY_MANAGER);

    private String path;
    private EarlyItemData data;

    public CogEarlyItem(String path) {
        this.path = path;
        this.data = new EarlyItemData();
    }

    public void register(DeferredRegister<Item> registry) {
        registry.register(path, data::sup);
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
        return o instanceof CogEarlyManager;
    }
}
