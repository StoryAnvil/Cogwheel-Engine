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

package com.storyanvil.cogwheel.entity;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryActionQueueAccessor;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Describes NPC
 */
public interface AbstractNPC<T extends Entity & AbstractNPC<T>> extends StoryActionQueueAccessor<T> {
    default void npc$setName(String name) {
        npc$getStoryEntity().storyEntity$putString("name", name)
                .storyEntity$syncIfOnServer();
    }
    default void npc$setNameLocal(String name) {
        npc$getStoryEntity().storyEntity$putString("name", name);
    }
    default String npc$getName() {
        return npc$getStoryEntity().storyEntity$getString("name", "NPC");
    }
    default void npc$setSkin(String name) {
        npc$getStoryEntity().storyEntity$putString("skin", name)
                .storyEntity$syncIfOnServer();
    }
    default void npc$setSkinLocal(String name) {
        npc$getStoryEntity().storyEntity$putString("skin", name);
    }
    default String npc$getSkin() {
        return npc$getStoryEntity().storyEntity$getString("skin", "test");
    };
    default void npc$setModel(String name) {
        npc$getStoryEntity().storyEntity$putString("model", name)
                .storyEntity$syncIfOnServer();
    }
    default void npc$setModelLocal(String name) {
        npc$getStoryEntity().storyEntity$putString("model", name);
    }
    default String npc$getModel() {
        return npc$getStoryEntity().storyEntity$getString("model", "npc");
    }
    default void npc$setDefaultPhoto(String name) {
        npc$getStoryEntity().storyEntity$putString("photo", name)
                .storyEntity$syncIfOnServer();
    }
    default void npc$setDefaultPhotoLocal(String name) {
        npc$getStoryEntity().storyEntity$putString("photo", name);
    }
    default String npc$getDefaultPhoto() {
        return npc$getStoryEntity().storyEntity$getString("photo", "empty");
    }
    @Api.ServerSideOnly
    void npc$chat(Text text);
    EntityNavigation npc$getNavigation();
    Identifier npc$getAnimatorID();
    default boolean npc$equalsCheckForAnimatorID(Identifier animatorID) {
        return npc$getAnimatorID().equals(animatorID);
    }
    @Api.ClientSideOnly
    void npc$pushAnimation(String name);

    T npc$getEntity();
    IStoryEntity npc$getStoryEntity();
}
