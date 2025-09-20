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

import com.storyanvil.cogwheel.items.CustomItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class EarlyItemData {
    private int stacksTo = 64;
    private int durability = -1;
    private Rarity rarity = Rarity.COMMON;
    private boolean noRepair = false;

    public Item sup() {
        return new CustomItem(createProperties());
    }
    private Item.Properties createProperties() {
        Item.Properties a = new Item.Properties();
        a.stacksTo(stacksTo);
        if (durability != -1) {
            a.durability(durability);
        }
        a.rarity(rarity);
        if (noRepair) {
            a.setNoRepair();
        }
        return a;
    }

    public void setStacksTo(int stacksTo) {
        if (stacksTo < 1 || stacksTo > 64) return;
        this.stacksTo = stacksTo;
    }

    public void setDurability(int durability) {
        if (durability < 0) return;
        this.durability = durability;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public void setNoRepair(boolean noRepair) {
        this.noRepair = noRepair;
    }
}
