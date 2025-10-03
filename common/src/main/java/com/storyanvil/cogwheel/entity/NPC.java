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

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryActionQueue;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryActionQueueAccessor;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntitySubscriber;
import com.storyanvil.cogwheel.network.mc.StoryEntitySync;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.util.DataStorage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NPC extends AnimalEntity implements AbstractNPC<NPC>, StoryActionQueueAccessor<NPC>, GeoEntity, IStoryEntitySubscriber {
    public NPC(EntityType<? extends AnimalEntity> pEntityType, World pLevel) {
        super(pEntityType, pLevel);
        if (pLevel.isClient) {
            CogwheelExecutor.scheduleTickEventClientSide(clientWorld -> {
                CogwheelHooks.sendPacketToServer(new StoryEntitySync(getUuidAsString(), new NbtCompound()));
            }, 10);
        } else {
            actionQueue = StoryActionQueue.importFromEntity(this);
            npc$setNameLocal(DataStorage.getString(this, "name", "NPC"));
            npc$setSkinLocal(DataStorage.getString(this, "skin", "test"));
            npc$setModelLocal(DataStorage.getString(this, "model", "npc"));
            npc$getStoryEntity().storyEntity$syncIfOnServer();
        }
        storyEntity$accept();
    }

    // TODO: RIGHT CLICK and LEFT CLICK

    // ========== === [ IStoryEntity ] === ========== \\
    @Override
    public void storyEntity$accept() {
        IStoryEntitySubscriber.super.storyEntity$accept();
        platformTexture = Identifier.of(CogwheelEngine.MODID, "textures/entity/npc/" + npc$getSkin() + ".png");
        platformModel = Identifier.of(CogwheelEngine.MODID, "geo/" + npc$getModel() + ".geo.json");
    }

    // ========== === [ Abstract NPC ] === ========== \\
    @Api.PlatformTool @Api.ClientSideOnly public Identifier platformModel = Identifier.of(CogwheelEngine.MODID, "geo/npc.geo.json");
    @Api.PlatformTool @Api.ClientSideOnly public Identifier platformTexture = Identifier.of(CogwheelEngine.MODID, "textures/entity/npc/test.png");
    @Api.ServerSideOnly private StoryActionQueue<NPC, ? extends NPC> actionQueue;

    @Override
    public NPC npc$getEntity() {
        return this;
    }

    @Override
    public IStoryEntity npc$getStoryEntity() {
        // IStoryEntity implementation is provided by per-platform mixins
        return (IStoryEntity) this;
    }

    @Override
    public void npc$setSkin(String name) {
        AbstractNPC.super.npc$setSkin(name);
        platformTexture = Identifier.of(CogwheelEngine.MODID, "textures/entity/npc/" + name + ".png");
    }

    @Override
    public void npc$setModel(String name) {
        AbstractNPC.super.npc$setModel(name);
        platformModel = Identifier.of(CogwheelEngine.MODID, "geo/" + name + ".geo.json");
    }

    @Override
    public Identifier npc$getAnimatorID() {
        return Identifier.of(CogwheelEngine.MODID, "npc/" + getUuidAsString());
    }

    @Override // This method is overridden to allow checks without creating new instance of Identifier
    public boolean npc$equalsCheckForAnimatorID(Identifier animatorID) {
        return animatorID.getNamespace().equals(CogwheelEngine.MODID) && animatorID.getPath().equals("npc/" + getUuidAsString());
    }

    @Override
    public EntityNavigation npc$getNavigation() {
        return this.getNavigation();
    }

    @Override
    public void npc$pushAnimation(String name) {
        if (name.equals("null")) {
            geoCustomAnimation = null;
            return;
        }
        geoCustomAnimation = RawAnimation.begin().thenLoop(name);
    }

    @Override
    public void npc$chat(Text text) {
        ServerWorld world = (ServerWorld) this.getWorld();
        Text msg = Text.literal("[").formatted(Formatting.GRAY)
                .append(Text.literal(npc$getName()).formatted(Formatting.AQUA))
                .append(Text.literal("]").formatted(Formatting.GRAY))
                .append(Text.literal(" ").formatted(Formatting.RESET, Formatting.WHITE))
                .append(text);
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(msg);
        }
    }

    @Override
    public <Q extends NPC> void addStoryAction(StoryAction<Q> action) {
        actionQueue.addStoryAction(action);
    }

    // ========== === [ Geckolib ] === ========== \\
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation geoWALK_ANIM = RawAnimation.begin().thenLoop("animation.npc.walk");
    protected static RawAnimation geoCustomAnimation = null;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walking", this::walkAnim));
    }

    protected <E extends NPC> PlayState walkAnim(final AnimationState<E> event) {
        if (geoCustomAnimation != null) {
            return event.setAndContinue(geoCustomAnimation);
        }
        if (event.isMoving())
            return event.setAndContinue(geoWALK_ANIM);

        return PlayState.STOP; //TODO: Idle animation
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // ========== === [ Vanilla methods ] === ========== \\
    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @ApiStatus.Internal
    public static DefaultAttributeContainer.Builder createAttributes() {
        return AnimalEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20)
                .add(EntityAttributes.FOLLOW_RANGE, 24D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.10000000149011612);
    }

    @Override
    public void checkDespawn() {
        // NPC will never despawn
    }

    @Override
    public void setCustomName(@Nullable Text pName) {
        npc$setName(pName == null ? "" : pName.getString());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            actionQueue.tick();
        }
    }

    @Override
    public @Nullable Text getCustomName() {
        return Text.literal(npc$getName());
    }
}
