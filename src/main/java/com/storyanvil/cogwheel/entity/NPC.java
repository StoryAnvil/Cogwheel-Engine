package com.storyanvil.cogwheel.entity;

import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.util.DataStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public class NPC extends Animal {
    public NPC(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        if (!pLevel.isClientSide) {
            setSkin(DataStorage.getString(this, "skin", "test"));
            setCustomName(DataStorage.getString(this, "name", "NPC"));
        }
    }

    public final AnimationState idle = new AnimationState();
    private int idleAnimTimeout = 0;
    private Queue<StoryAction<NPC>> actionQueue = new ArrayDeque<>();
    private StoryAction<NPC> current;

    private static EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(NPC.class, EntityDataSerializers.STRING);
    private static EntityDataAccessor<String> NAME = SynchedEntityData.defineId(NPC.class, EntityDataSerializers.STRING);

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            setupState();
        } else {
            if (current != null) {
                if (current.freeToGo(this)) {
                    current = null;
                }
            } else {
                if (actionQueue.isEmpty()) return;
                current = actionQueue.remove();
                current.proceed(this);
            }
        }
    }


    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6f, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    public @Nullable Component getCustomName() {
        return Component.literal(this.entityData.get(NAME));
    }
    public String getCogName() {
        return this.entityData.get(NAME);
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        this.entityData.set(NAME, pName == null ? "NPC" : pName.getString(), true);
    }
    public void setCustomName(@Nullable String pName) {
        this.entityData.set(NAME, pName == null ? "NPC" : pName, true);
    }

    private void setupState() {
        if (this.idleAnimTimeout <= 0) {
            this.idleAnimTimeout = this.random.nextInt(40) + 80;
            this.idle.start(this.tickCount);
        } else {
            --this.idleAnimTimeout;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612);
    }

    public Queue<StoryAction<NPC>> getActionQueue() {
        return actionQueue;
    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 5f));
    }

    @Nullable @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN, "test");
        this.entityData.define(NAME, "NPC");
    }

    @Override
    public void checkDespawn() {
        // NPC will never despawn
    }

    public String getSkin() {
        return this.entityData.get(SKIN);
    }
    public void setSkin(String skin) {
        this.entityData.set(SKIN, skin, true);
    }
}
