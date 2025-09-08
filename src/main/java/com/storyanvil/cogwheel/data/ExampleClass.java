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

package com.storyanvil.cogwheel.data;

public class ExampleClass {
    public static final StoryCodec<ExampleClass> CODEC = StoryCodecBuilder.build(
            StoryCodecBuilder.Prop(ExampleClass::getName, StoryCodecs.STRING),
            StoryCodecBuilder.Prop(ExampleClass::getAge, StoryCodecs.INTEGER),
            StoryCodecBuilder.Prop(ExampleClass::isAdmin, StoryCodecs.BOOLEAN),
            ExampleClass::new
    );

    private String name;
    private int age;
    private boolean isAdmin;

    public ExampleClass(String name, int age, boolean isAdmin) {
        this.name = name;
        this.age = age;
        this.isAdmin = isAdmin;
    }
    public ExampleClass() {
        this.name = null;
        this.age = 0;
        this.isAdmin = false;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public boolean isAdmin() {
        return isAdmin;
    }
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
