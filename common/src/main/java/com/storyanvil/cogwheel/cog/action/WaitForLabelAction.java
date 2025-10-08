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

package com.storyanvil.cogwheel.cog.action;

import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import com.storyanvil.cogwheel.util.CogwheelExecutor;

import java.util.function.BiConsumer;

public class WaitForLabelAction extends StoryAction<Object> implements BiConsumer<String, StoryAction<?>> {
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
        CogwheelExecutor.getDefaultEnvironment().registerLabelListener(label, this);
    }

    @Override
    public boolean freeToGo(Object myself) {
        return label == null;
    }

    @Override
    public void accept(String s, StoryAction<?> storyAction) {
        amount--;
        if (amount <= 0) this.label = null;
    }
}
