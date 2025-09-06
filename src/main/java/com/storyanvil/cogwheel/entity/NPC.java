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
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.StoryAction;
import com.storyanvil.cogwheel.infrastructure.abilities.*;
import com.storyanvil.cogwheel.infrastructure.actions.AnimationAction;
import com.storyanvil.cogwheel.infrastructure.actions.PathfindAction;
import com.storyanvil.cogwheel.infrastructure.actions.WaitForLabelAction;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.network.mc.AnimationBound;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.DialogBound;
import com.storyanvil.cogwheel.network.mc.DialogChoiceBound;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NPC extends Animal implements
        StoryActionQueue<NPC>, StoryChatter, StoryNameHolder, StorySkinHolder,
        StoryNavigator, ObjectMonitor.IMonitored, CogPropertyManager,
        StoryAnimator, GeoEntity, StoryModel, DialogTarget {
    private static final ObjectMonitor<NPC> MONITOR = new ObjectMonitor<>();
    public NPC(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        MONITOR.register(this);
        if (!pLevel.isClientSide) {
            me = new CogEntity(this);
            setSkin(DataStorage.getString(this, "skin", "test"));
            setCustomName(DataStorage.getString(this, "name", "NPC"));
            setStoryModelID(DataStorage.getString(this, "model", "npc"));
        }
    }

    private final Queue<StoryAction<? extends NPC>> actionQueue = new ArrayDeque<>();
    private StoryAction current;
    private CogEntity me;

    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(NPC.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(NPC.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> MODEL = SynchedEntityData.defineId(NPC.class, EntityDataSerializers.STRING);

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
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


//    @Override
//    protected void updateWalkAnimation(float pPartialTick) {
//        float f;
//        if (this.getPose() == Pose.STANDING) {
//            f = Math.min(pPartialTick * 6f, 1f);
//        } else {
//            f = 0f;
//        }
//
//        this.walkAnimation.update(f, 0.2f);
//    }

    @Override @Api.Stable(since = "2.0.0")
    public @Nullable Component getCustomName() {
        return Component.literal(this.entityData.get(NAME));
    }

    @Override @Api.Stable(since = "2.0.0")
    public String getCogName() {
        return this.entityData.get(NAME);
    }
    @Override @Api.Stable(since = "2.0.0")
    public void setCogName(String name) {
        this.entityData.set(NAME, name, true);
        if (!level().isClientSide) {
            DataStorage.setString(this, "name", name);
        }
    }

    @Override @Api.Stable(since = "2.0.0")
    public void setCustomName(@Nullable Component pName) {
        this.entityData.set(NAME, pName == null ? "NPC" : pName.getString(), true);
    }
    @Api.Stable(since = "2.0.0")
    public void setCustomName(@Nullable String pName) {
        this.entityData.set(NAME, pName == null ? "NPC" : pName, true);
    }

//    private void setupState() {
//        if (this.idleAnimTimeout <= 0) {
//            this.idleAnimTimeout = this.random.nextInt(40) + 80;
//            this.idle.start(this.tickCount);
//        } else {
//            --this.idleAnimTimeout;
//        }
//    }

    @Api.Internal @ApiStatus.Internal
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612);
    }

    @Override @ApiStatus.Internal @Api.Internal
    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 5f));
    }

    @Nullable @Override @Api.Internal @ApiStatus.Internal
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN, "test");
        this.entityData.define(NAME, "NPC");
        this.entityData.define(MODEL, "npc");
    }

    @Override
    public void checkDespawn() {
        // NPC will never despawn
    }

    @Api.Stable(since = "2.0.0")
    public String getSkin() {
        return this.entityData.get(SKIN);
    }

    @Api.Stable(since = "2.0.0")
    public void setSkin(String skin) {
        this.entityData.set(SKIN, skin, true);
        if (!level().isClientSide) {
            DataStorage.setString(this, "skin", skin);
        }
    }

    @Override @Api.Stable(since = "2.0.0")
    public void chat(String text) {
        if (!this.level().isClientSide) {
            Component c = Component.literal("[" + getCogName() + "] " + text);
            for (ServerPlayer player : ((ServerLevel) this.level()).players()) {
                player.sendSystemMessage(c);
            }
        }
    }

    @Api.Stable(since = "2.0.0")
    public <R> void addStoryAction(StoryAction<R> action) {
        actionQueue.add((StoryAction<? extends NPC>) action);
    }

    @Override @Api.Stable(since = "2.0.0")
    public void reportState(StringBuilder sb) {
        for (StoryAction<?> action : actionQueue) {
            sb.append(action.toString());
        }
        sb.append(">").append(current.toString());
        sb.append(" | ").append(this);
    }

    private static final EasyPropManager MANAGER = new EasyPropManager("npc", NPC::registerProps);

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

        manager.reg("setModel", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            return npc.addChained(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    myself.setStoryModelID(args.get(0).convertToString());
                }
            });
        });
        manager.reg("getModel", (name, args, script, o) -> {
            return new CogString(((NPC) o).getStoryModelID());
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

        manager.reg("animation", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            return npc.addChained(new AnimationAction(args.getString(0), args.requireInt(1)));
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
                    // Dialogs can be registered only in default environment
                    CogwheelExecutor.getDefaultEnvironment().registerDialog(dialogID, response -> {
                        CogwheelExecutor.schedule(() -> {
                            script.put(variable, new CogInteger(response));
                            script.lineDispatcher();
                        });
                    });
                }
            });
        });
        manager.reg("dialogBlocking", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            DialogBound bound = DialogBound.tell(args.getString(0), npc.getCogName(), args.getString(1));
            npc.addStoryAction(new StoryAction.Ticking<NPC>(args.requireInt(2)) {
                @Override
                public void proceed(NPC myself) {
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), bound);
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(myself.getAnimatorID(), "animation.npc.talk"));
                }
            });
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    CogwheelExecutor.scheduleTickEvent(e -> {
                        script.put(variable, CogPropertyManager.nullManager);
                        CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), DialogBound.close());
                        if (CogwheelConfig.isNpcTalkingAnimationEnabled())
                            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(npc.getAnimatorID(), "null"));
                        CogwheelExecutor.schedule(script::lineDispatcher);
                    }, args.requireInt(2));
                }
            });
        });
        manager.reg("dialogNonBlocking", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            DialogBound bound = DialogBound.tell(args.getString(0), npc.getCogName(), args.getString(1));
            // TODO add to wiki
            return npc.addChained(new StoryAction.Ticking<NPC>(args.requireInt(2)) {
                @Override
                public void proceed(NPC myself) {
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), bound);
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(myself.getAnimatorID(), "animation.npc.talk"));
                }

                @Override
                public void onEnding() {
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), DialogBound.close());
                    if (CogwheelConfig.isNpcTalkingAnimationEnabled())
                        CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(npc.getAnimatorID(), "null"));
                }
            });
        });
        manager.reg("dialogChoiceUI", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            final String dialogID = UUID.randomUUID().toString();
            int optionsLength = args.size() - 1;
            String[] options = new String[optionsLength - 1];
            for (int i = 0; i < optionsLength - 1; i++) {
                options[i] = args.getString(i + 1);
            }
            DialogChoiceBound bound = DialogChoiceBound.choice(dialogID, args.getString(0), options, npc.getCogName(), args.getString(optionsLength));
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogwheelExecutor.scheduleTickEvent(event -> {
                        CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), bound);
                    });
                }
            });
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    // Dialogs can be registered only in default environment
                    CogwheelExecutor.getDefaultEnvironment().registerDialog(dialogID, response -> {
                        CogwheelExecutor.schedule(() -> {
                            // Send dialog close packet
                            CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                                CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), DialogChoiceBound.close());
                            });
                            script.put(variable, new CogInteger(response));
                            CogwheelExecutor.schedule(script::lineDispatcher);
                        });
                    });
                }
            });
        });
    }

    @Override @Api.Stable(since = "2.0.0")
    public boolean hasOwnProperty(String name) {
        if (me.hasOwnProperty(name)) return true;
        return MANAGER.hasOwnProperty(name);
    }

    @Override @Api.Stable(since = "2.0.0")
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        if (me.hasOwnProperty(name)) {
            return me.getProperty(name, args, script);
        }
        return MANAGER.get(name).handle(name, args, script, me());
    }

    @Override @Api.Stable(since = "2.0.0")
    public boolean equalsTo(CogPropertyManager o) {
        return false;
    }

    @Contract(value = " -> this", pure = true)
    private NPC me() {
        return this;
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.npc.walk");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walking", this::walkAnim));
    }

    protected <E extends NPC> PlayState walkAnim(final AnimationState<E> event) {
        if (customAnimation != null) {
            return event.setAndContinue(customAnimation);
        }
        if (event.isMoving())
            return event.setAndContinue(WALK_ANIM);

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public String getAnimatorID() {
        return this.getStringUUID();
    }

    private RawAnimation customAnimation = null;

    @Override @Api.Stable(since = "2.0.0")
    public void pushAnimation(String name) {
        if (name.equals("null")) {
            customAnimation = null;
            return;
        }
        customAnimation = RawAnimation.begin().thenLoop(name);
    }

    @Override
    public String getStoryModelID() {
        return this.entityData.get(MODEL);
    }

    @Override
    public void setStoryModelID(String id) {
        this.entityData.set(MODEL, id, true);
        if (!level().isClientSide) {
            DataStorage.setString(this, "model", id);
        }
    }

    @Override
    public void d$say(String text, String texture, int ticks, Runnable trigger) {
        DialogBound bound = DialogBound.tell(text, getCogName(), texture);
        this.addStoryAction(new StoryAction.Ticking<NPC>(ticks) {
            @Override
            public void proceed(NPC myself) {
                CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), bound);
                CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(myself.getAnimatorID(), "animation.npc.talk"));
            }

            @Override
            public void onEnding() {
                CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), DialogBound.close());
                if (CogwheelConfig.isNpcTalkingAnimationEnabled())
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(NPC.this.getAnimatorID(), "null"));
                CogwheelExecutor.schedule(trigger);
            }
        });
    }

    @Override
    public void d$ask(String text, String texture, List<String> options, Consumer<Integer> acceptor) {
        final String dialogID = UUID.randomUUID().toString();
        DialogChoiceBound bound = DialogChoiceBound.choice(dialogID, text, options, this.getCogName(), texture);
        this.addStoryAction(new StoryAction.Instant<NPC>() {
            @Override
            public void proceed(NPC myself) {
                CogwheelExecutor.scheduleTickEvent(event -> {
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), bound);
                });
            }
        });
        CogwheelExecutor.getDefaultEnvironment().registerDialog(dialogID, response -> {
            CogwheelExecutor.schedule(() -> {
                // Send dialog close packet
                CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                    CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), DialogChoiceBound.close());
                });
                acceptor.accept(response);
            });
        });
    }

    @Override
    public String d$name() {
        return getCogName();
    }
}
