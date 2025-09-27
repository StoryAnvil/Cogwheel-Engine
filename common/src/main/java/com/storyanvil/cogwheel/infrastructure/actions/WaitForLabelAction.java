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

package com.storyanvil.cogwheel.infrastructure.actions;

import com.google.gson.JsonObject;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.infrastructure.StoryAction;
import com.storyanvil.cogwheel.util.LabelCloseable;

public class WaitForLabelAction extends StoryAction<Object> implements LabelCloseable {
    private String label;
    private int amount;

    public WaitForLabelAction(String label, int amount) {
        super();
        this.amount = amount;
        this.label = label;
    }

    public WaitForLabelAction(String label) {
        super();
        this.label = label;
        this.amount = 1;
    }

    @Override
    public void proceed(Object myself) {
        EventBus.register(label, this);
    }

    @Override
    public boolean freeToGo(Object myself) {
        return label == null;
    }

    @Override
    public void close(String label, StoryAction<?> host) {
        amount--;
        if (amount <= 0) this.label = null;
    }

    @Override
    protected void toJSON(JsonObject obj) {
        super.toJSON(obj);
        obj.addProperty("label", label);
        obj.addProperty("amount", amount);
    }
}
