/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.entity;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.*;
import com.storyanvil.cogwheel.infrustructure.actions.PathfindAction;
import com.storyanvil.cogwheel.infrustructure.actions.WaitForLabelAction;
import com.storyanvil.cogwheel.infrustructure.cog.*;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
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
import java.util.UUID;

public class NPC extends Animal implements
        StoryActionQueue<NPC>, StoryChatter, StoryNameHolder, StorySkinHolder,
        StoryNavigator, ObjectMonitor.IMonitored, CogPropertyManager {
    private static final ObjectMonitor<NPC> MONITOR = new ObjectMonitor<>();
    public NPC(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        MONITOR.register(this);
        if (!pLevel.isClientSide) {
            me = new CogEntity(this);
            setSkin(DataStorage.getString(this, "skin", "test"));
            setCustomName(DataStorage.getString(this, "name", "NPC"));
        }
    }

    public final AnimationState idle = new AnimationState();
    private int idleAnimTimeout = 0;
    private Queue<StoryAction<? extends NPC>> actionQueue = new ArrayDeque<>();
    private StoryAction current;
    private CogEntity me;

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

    public <R> void addStoryAction(StoryAction<R> action) {
        actionQueue.add((StoryAction<? extends NPC>) action);
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
            return npc.addChained(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.setCogName(args.get(0).convertToString());
                }
            });
        });
        manager.reg("getName", (name, args, script, o) -> {
            return new CogString(((NPC) o).getCogName());
        });

        manager.reg("setSkin", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            return npc.addChained(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.setSkin(args.get(0).convertToString());
                }
            });
        });
        manager.reg("getSkin", (name, args, script, o) -> {
            return new CogString(((NPC) o).getSkin());
        });

        manager.reg("chat", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            return npc.addChained(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.chat(args.get(0).convertToString());
                }
            });
        });

        manager.reg("pathfind", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            return npc.addChained(new PathfindAction(new BlockPos(
                    args.requireInt(0), args.requireInt(1), args.requireInt(2)
            )));
        });

        manager.reg("waitForLabel", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            if (args.size() == 1)
                return npc.addChained(new WaitForLabelAction(args.getString(0)));
            else return npc.addChained(new WaitForLabelAction(args.getString(0), args.requireInt(1)));
        });

        manager.reg("dialogChoices", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            final String dialogID = UUID.randomUUID().toString();
            Component[] components = new Component[args.size()];
            for (int i = 0; i < components.length; i++) {
                final int finalI = i;
                components[i] = Component.literal("[" + (i + 1) + "] ").withStyle(style -> style.withColor(ChatFormatting.GRAY))
                        .append(Component.literal(args.getString(i)).withStyle(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/@storyclient dialog " + finalI + " " + dialogID)
                        ).withColor(ChatFormatting.WHITE)));
            }
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogwheelExecutor.scheduleTickEvent(event -> {
                        StoryUtils.sendGlobalMessage((ServerLevel) event.level, components);
                    });
                }
            });
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    EventBus.registerDialog(dialogID, response -> {
                        CogwheelExecutor.schedule(() -> {
                            script.put(variable, new CogInteger(response));
                            script.lineDispatcher();
                        });
                    });
                }
            });
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        if (me.hasOwnProperty(name)) return true;
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        if (me.hasOwnProperty(name)) {
            return me.getProperty(name, args, script);
        }
        return MANAGER.get(name).handle(name, args, script, me());
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return me.equalsTo(o);
    }

    @Contract(value = " -> this", pure = true)
    private NPC me() {
        return this;
    }
}
