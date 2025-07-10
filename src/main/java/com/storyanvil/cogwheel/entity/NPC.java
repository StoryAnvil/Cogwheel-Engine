/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.entity;

import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.*;
import com.storyanvil.cogwheel.infrustructure.cog.CogString;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public class NPC extends Animal implements
        StoryActionQueue<NPC>, StoryChatter, StoryNameHolder, StorySkinHolder,
        StoryNavigator, ObjectMonitor.IMonitored, CogPropertyManager {
    private static final ObjectMonitor<NPC> MONITOR = new ObjectMonitor<>();
    public NPC(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        MONITOR.register(this);
        if (!pLevel.isClientSide) {
            setSkin(DataStorage.getString(this, "skin", "test"));
            setCustomName(DataStorage.getString(this, "name", "NPC"));
        }
    }

    public final AnimationState idle = new AnimationState();
    private int idleAnimTimeout = 0;
    private Queue<StoryAction<? extends NPC>> actionQueue = new ArrayDeque<>();
    private StoryAction current;

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

    @Override
    public String getCogName() {
        return this.entityData.get(NAME);
    }
    @Override
    public void setCogName(String name) {
        this.entityData.set(NAME, name, true);
        if (!level().isClientSide) {
            DataStorage.setString(this, "name", name);
        }
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
        if (!level().isClientSide) {
            DataStorage.setString(this, "skin", skin);
        }
    }

    @Override
    public void chat(String text) {
        if (!this.level().isClientSide) {
            Component c = Component.literal("[" + getCogName() + "] " + text);
            for (ServerPlayer player : ((ServerLevel) this.level()).players()) {
                player.sendSystemMessage(c);
            }
        }
    }

    @Override
    public <R extends NPC> void addStoryAction(StoryAction<R> action) {
        actionQueue.add(action);
    }

    @Override
    public Queue<StoryAction<? extends NPC>> getActions() {
        return actionQueue;
    }

    @Override
    public void reportState(StringBuilder sb) {
        for (StoryAction<?> action : actionQueue) {
            sb.append(action.toString());
        }
        sb.append(">").append(current.toString());
        sb.append(" | ").append(this);
    }

    private static EasyPropManager MANAGER = new EasyPropManager("npc", NPC::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("setName", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.setCogName(args);
                }
            });
            return null;
        });
        manager.reg("setNameV", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogPropertyManager m = script.get(args);
                    if (m instanceof CogString str) {
                        myself.setCogName(str.getValue());
                    } else throw new RuntimeException(args + " variable doesn't store CogString");
                }
            });
            return null;
        });
        manager.reg("getName", (name, args, script, o) -> {
            return new CogString(((NPC) o).getCogName());
        });

        manager.reg("setSkin", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.setSkin(args);
                }
            });
            return null;
        });
        manager.reg("setSkinV", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogPropertyManager m = script.get(args);
                    if (m instanceof CogString str) {
                        myself.setSkin(str.getValue());
                    } else throw new RuntimeException(args + " variable doesn't store CogString");
                }
            });
            return null;
        });
        manager.reg("getSkin", (name, args, script, o) -> {
            return new CogString(((NPC) o).getSkin());
        });

        manager.reg("chat", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.chat(args);
                }
            });
            return null;
        });
        manager.reg("chatV", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogPropertyManager m = script.get(args);
                    if (m instanceof CogString str) {
                        myself.chat(str.getValue());
                    } else throw new RuntimeException(args + " variable doesn't store CogString");
                }
            });
            return null;
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, String args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, me());
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o == this;
    }

    @Contract(value = " -> this", pure = true)
    private NPC me() {
        return this;
    }
}
