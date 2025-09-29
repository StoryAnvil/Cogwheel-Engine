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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.StoryAction;
import com.storyanvil.cogwheel.infrastructure.abilities.*;
import com.storyanvil.cogwheel.infrastructure.actions.AnimationAction;
import com.storyanvil.cogwheel.infrastructure.actions.PathfindAction;
import com.storyanvil.cogwheel.infrastructure.actions.WaitForLabelAction;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import com.storyanvil.cogwheel.network.devui.DevOpenViewer;
import com.storyanvil.cogwheel.network.devui.inspector.InspectableEntity;
import com.storyanvil.cogwheel.network.mc.AnimationBound;
import com.storyanvil.cogwheel.network.mc.DialogBound;
import com.storyanvil.cogwheel.network.mc.DialogChoiceBound;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NPC extends AnimalEntity implements
        StoryActionQueue<NPC>, StoryChatter, StoryNameHolder, StorySkinHolder,
        StoryNavigator, ObjectMonitor.IMonitored, CGPM,
        StoryAnimator, GeoEntity, StoryModel, DialogTarget, InspectableEntity {
    private static final ObjectMonitor<NPC> MONITOR = new ObjectMonitor<>();
    public NPC(EntityType<? extends AnimalEntity> pEntityType, World pLevel) {
        super(pEntityType, pLevel);
        MONITOR.register(this);
        if (!pLevel.isClient) {
            me = new CogEntity(this);
            setSkin(DataStorage.getString(this, "skin", "test"));
            setCustomName(DataStorage.getString(this, "name", "NPC"));
            setStoryModelID(DataStorage.getString(this, "model", "npc"));
            storyEntity().storyEntity$putString("rightclick", storyEntity().storyEntity$getString("rightclick", ""));
            storyEntity().storyEntity$putString("leftclick", storyEntity().storyEntity$getString("leftclick", ""));
        }
    }

    public IStoryEntity storyEntity() {
        return (IStoryEntity) this;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    private final Queue<StoryAction<? extends NPC>> actionQueue = new ArrayDeque<>();
    private StoryAction current;
    private CogEntity me;
    private long lastInteraction = 0;

    @Api.PlatformTool
    public Identifier platformModel = Identifier.of(CogwheelEngine.MODID, "geo/npc.geo.json");
    @Api.PlatformTool
    public Identifier platformTexture = Identifier.of(CogwheelEngine.MODID, "textures/entity/npc/test.png");

    @Override
    public @NotNull ActionResult interactMob(@NotNull PlayerEntity plr, @NotNull Hand hand) {
        String action = storyEntity().storyEntity$getString("rigthclick" ,"");
        if (plr instanceof ServerPlayerEntity player && !action.isEmpty()) {
            long time = System.currentTimeMillis();
            if (time - lastInteraction < 250) {
                return ActionResult.FAIL;
            }
            lastInteraction = time;
            CogScriptEnvironment.dispatchScriptGlobal(action, new ScriptStorage()
                    .append("internal_callback", new CogEventCallback())
                    .append("event_player", new CogPlayer(new WeakReference<>(player)))
                    .append("event_npc", this));
            return ActionResult.CONSUME;
        }
        return super.interactMob(plr, hand);
    }

    @SuppressWarnings("unused")
    public void interact(ServerPlayerEntity plr) {
        String action = storyEntity().storyEntity$getString("leftclick", "");
        if (action.isEmpty()) return;
        long time = System.currentTimeMillis();
        if (time - lastInteraction < 250) {
            return;
        }
        lastInteraction = time;
        CogScriptEnvironment.dispatchScriptGlobal(action, new ScriptStorage()
                .append("internal_callback", new CogEventCallback())
                .append("event_player", new CogPlayer(new WeakReference<>(plr)))
                .append("event_npc", this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
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

    @Override @Api.Stable(since = "2.0.0")
    public @Nullable Text getCustomName() {
        return Text.literal(getCogName());
    }

    @Override @Api.Stable(since = "2.0.0")
    public String getCogName() {
        return storyEntity().storyEntity$getString("name", "NPC");
    }
    @Override @Api.Stable(since = "2.0.0")
    public void setCogName(String name) {
        storyEntity().storyEntity$putString("name", name);
    }

    @Override @Api.Stable(since = "2.0.0")
    public void setCustomName(@Nullable Text pName) {
        storyEntity().storyEntity$putString("name", pName == null ? "" : pName.getString());
    }
    @Api.Stable(since = "2.0.0")
    public void setCustomName(@Nullable String pName) {
        storyEntity().storyEntity$putString("name", pName == null ? "" : pName);
    }

    @Api.Internal @ApiStatus.Internal
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

    @Override @Api.Stable(since = "2.0.0")
    public String getSkin() {
        return storyEntity().storyEntity$getString("skin", "test");
    }

    @Override @Api.Stable(since = "2.0.0")
    public void setSkin(String skin) {
        storyEntity().storyEntity$putString("skin", skin);
        platformTexture = Identifier.of(CogwheelEngine.MODID, "textures/entity/npc/" + getSkin() + ".png");
    }

    @Override @Api.Stable(since = "2.0.0")
    public void chat(String text) {
        if (!this.getWorld().isClient) {
            Text c = Text.literal("[" + getCogName() + "] " + text);
            for (ServerPlayerEntity player : ((ServerWorld) this.getWorld()).getPlayers()) {
                player.sendMessage(c);
            }
        }
    }

    @Api.Stable(since = "2.0.0")
    public synchronized <R> void addStoryAction(StoryAction<R> action) {
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
            Text[] Texts = new Text[args.size()];
            for (int i = 0; i < Texts.length; i++) {
                Texts[i] = Text.literal("[" + (i + 1) + "] ").fillStyle(Style.EMPTY.withColor(Formatting.GRAY))
                        .append(Text.literal(args.getString(i)).fillStyle(Style.EMPTY.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/@storyclient dialog " + i + " " + dialogID)
                        ).withColor(Formatting.WHITE)));
            }
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogwheelExecutor.scheduleTickEvent(event -> {
                        StoryUtils.sendGlobalMessage(event, Texts);
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
            DialogBound bound = new DialogBound(args.getString(0), npc.getCogName(), args.getString(1));
            npc.addStoryAction(new StoryAction.Ticking<NPC>(args.requireInt(2)) {
                @Override
                public void proceed(NPC myself) {
                    CogwheelHooks.sendPacketToEveryone(bound);
                    CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.getAnimatorID(), "animation.npc.talk"));
                }
            });
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    CogwheelExecutor.scheduleTickEvent(e -> {
                        script.put(variable, CGPM.nullManager);
                        CogwheelHooks.sendPacketToEveryone(new DialogBound());
                        if (CogwheelConfig.isNpcTalkingAnimationEnabled())
                            CogwheelHooks.sendPacketToEveryone(new AnimationBound(npc.getAnimatorID(), "null"));
                        CogwheelExecutor.schedule(script::lineDispatcher);
                    }, args.requireInt(2));
                }
            });
        });
        manager.reg("dialogNonBlocking", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            DialogBound bound = new DialogBound(args.getString(0), npc.getCogName(), args.getString(1));
            return npc.addChained(new StoryAction.Ticking<NPC>(args.requireInt(2)) {
                @Override
                public void proceed(NPC myself) {
                    CogwheelHooks.sendPacketToEveryone(bound);
                    CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.getAnimatorID(), "animation.npc.talk"));
                }

                @Override
                public void onEnding() {
                    CogwheelHooks.sendPacketToEveryone(new DialogBound());
                    if (CogwheelConfig.isNpcTalkingAnimationEnabled())
                        CogwheelHooks.sendPacketToEveryone(new AnimationBound(npc.getAnimatorID(), "null"));
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
            DialogChoiceBound bound = new DialogChoiceBound(dialogID, args.getString(0), options, npc.getCogName(), args.getString(optionsLength));
            npc.addStoryAction(new StoryAction.Instant<NPC>() {
                @Override
                public void proceed(NPC myself) {
                    CogwheelExecutor.scheduleTickEvent(event -> {
                        CogwheelHooks.sendPacketToEveryone(bound);
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
                                CogwheelHooks.sendPacketToEveryone(new DialogChoiceBound());
                            });
                            script.put(variable, new CogInteger(response));
                            CogwheelExecutor.schedule(script::lineDispatcher);
                        });
                    });
                }
            });
        });
        manager.reg("setRightClick", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                npc.storyEntity().storyEntity$putString("rightclick", args.getString(0));
            });
            return null;
        });
        manager.reg("setLeftClick", (name, args, script, o) -> {
            NPC npc = (NPC) o;
            CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                npc.storyEntity().storyEntity$putString("leftclick", args.getString(0));
            });
            return null;
        });
    }

    @Override @Api.Stable(since = "2.0.0")
    public boolean hasOwnProperty(String name) {
        if (me.hasOwnProperty(name)) return true;
        return MANAGER.hasOwnProperty(name);
    }

    @Override @Api.Stable(since = "2.0.0")
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) {
        if (me.hasOwnProperty(name)) {
            return me.getProperty(name, args, script);
        }
        return MANAGER.get(name).handle(name, args, script, NPC.this);
    }

    @Override @Api.Stable(since = "2.0.0")
    public boolean equalsTo(CGPM o) {
        return false;
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.npc.walk");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walking", this::walkAnim));
    }

    protected <E extends NPC> PlayState walkAnim(final AnimationState<E> event) {
//        System.out.println(event.animationTick + " | " + event.getController().getAnimationState().toString());
        if (customAnimation != null) {
            return event.setAndContinue(customAnimation);
        }
        if (event.isMoving())
            return event.setAndContinue(WALK_ANIM);

        return PlayState.STOP; //TODO: Idle animation
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public String getAnimatorID() {
        return this.getUuidAsString();
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
        return storyEntity().storyEntity$getString("model", "npc");
    }

    @Override
    public void setStoryModelID(String id) {
        storyEntity().storyEntity$putString("model", id);
//        platformModel = Identifier.of(CogwheelEngine.MODID, "geo/entity/" + getStoryModelID() + ".geo.json");
    }

    @Override
    public void d$say(String text, String texture, int ticks, Runnable trigger) {
        DialogBound bound = new DialogBound(text, getCogName(), texture);
        this.addStoryAction(new StoryAction.Ticking<NPC>(ticks) {
            @Override
            public void proceed(NPC myself) {
                CogwheelHooks.sendPacketToEveryone(bound);
                CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.getAnimatorID(), "animation.npc.talk"));
            }

            @Override
            public void onEnding() {
                CogwheelHooks.sendPacketToEveryone(new DialogBound());
                if (CogwheelConfig.isNpcTalkingAnimationEnabled())
                    CogwheelHooks.sendPacketToEveryone(new AnimationBound(NPC.this.getAnimatorID(), "null"));
                CogwheelExecutor.schedule(trigger);
            }
        });
    }

    @Override
    public void d$ask(String text, String texture, List<String> options, Consumer<Integer> acceptor) {
        final String dialogID = UUID.randomUUID().toString();
        DialogChoiceBound bound = new DialogChoiceBound(dialogID, text, options, this.getCogName(), texture);
        this.addStoryAction(new StoryAction.Instant<NPC>() {
            @Override
            public void proceed(NPC myself) {
                CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                    CogwheelHooks.sendPacketToEveryone(bound);
                });
            }
        });
        CogwheelExecutor.getDefaultEnvironment().registerDialog(dialogID, response -> {
            CogwheelExecutor.schedule(() -> {
                // Send dialog close packet
                CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                    CogwheelHooks.sendPacketToEveryone(new DialogChoiceBound());
                });
                acceptor.accept(response);
            });
        });
    }

    @Override
    public String d$name() {
        return getCogName();
    }

    @Override
    public synchronized boolean tryToInspect(@NotNull ServerWorld level, @NotNull ServerPlayerEntity player) {
        JsonObject obj = new JsonObject();
        obj.addProperty("uuid", getUuidAsString());
        obj.addProperty("animatorID", getAnimatorID());
        obj.addProperty("skinID", getSkin());
        obj.addProperty("modelID", getStoryModelID());/*
        obj.addProperty("rightClick", this.entityData.get(RIGHT_CLICK));
        obj.addProperty("leftClick", this.entityData.get(LEFT_CLICK));*/// TODO: Universal entity inspector screen
        JsonArray array = new JsonArray();
        for (StoryAction<?> action : actionQueue) {
            array.add(action.toJSON());
        }
        obj.add("storyActions", array);
        if (current != null) {
            obj.add("currentStoryAction", current.toJSON());
        } else obj.add("currentStoryAction", null);

        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setStrictness(Strictness.LENIENT);
            jsonWriter.setIndent("    ");
            jsonWriter.setSerializeNulls(true);
            Streams.write(obj, jsonWriter);
            CogwheelHooks.sendPacket(new DevOpenViewer("npc.json", stringWriter.toString()), player);
            return true;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}
