package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.util.ActionFactory;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.LabelCloseable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

import java.util.HashMap;

public abstract class StoryAction<T> {
    private String actionLabel = null;
    public abstract void proceed(T myself);
    public abstract boolean freeToGo(T myself);
    public void setLabel(String newLabel) {
        this.actionLabel = newLabel;
    }
    public String getLabel() {
        return actionLabel;
    }
    void hitLabel() {
        if (actionLabel == null) return;
    }

    private static HashMap<String, ActionFactory> registry = null;
    public static void registerAction(String id, ActionFactory factory) {
        if (registry == null) registry = new HashMap<>(1);
        else if (registry.containsKey(id)) throw new RuntimeException(id + " StoryAction is already registered");
        registry.put(id, factory);
    }
    public static ActionFactory get(String id) {
        return registry.get(id);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "$ACT#";
    }

    public static abstract class NPCAction extends StoryAction<NPC> {}

    public static class Chat extends NPCAction {
        private Component text;

        public Chat(Component text) {
            this.text = text;
        }

        @Override
        public void proceed(NPC myself) {
            for (ServerPlayer p : ((ServerLevel) myself.level()).players()) {
                p.sendSystemMessage(Component.literal("[" + myself.getCogName() + "] ").append(text));
            }
            hitLabel();
        }
        @Override
        public boolean freeToGo(NPC myself) {
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + text;
        }
    }
    public static class PathfindTo extends NPCAction {
        private BlockPos target;

        public PathfindTo(BlockPos target) {
            this.target = target;
        }

        @Override
        public void proceed(NPC myself) {
            PathNavigation navigation = myself.getNavigation();
            Path path = navigation.createPath(target.getX(), target.getY(), target.getZ(), 0);
            myself.getNavigation().moveTo(path, 2);
        }
        @Override
        public boolean freeToGo(NPC myself) {
            boolean done = myself.getNavigation().isDone();
            if (done) hitLabel();
            return done;
        }

        @Override
        public String toString() {
            return super.toString() + target;
        }
    }
    public static class TeleportTo extends NPCAction {
        private BlockPos target;

        public TeleportTo(BlockPos target) {
            this.target = target;
        }

        @Override
        public void proceed(NPC myself) {
            myself.teleportToWithTicket(target.getX(), target.getY(), target.getZ());
            hitLabel();
        }
        @Override
        public boolean freeToGo(NPC myself) {
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + target;
        }
    }
    public static class WaitForLabelNPC extends NPCAction implements LabelCloseable {
        private String label;
        private int amount = 1;

        public WaitForLabelNPC(String label) {
            this.label = label;
        }

        public WaitForLabelNPC(String label, int amount) {
            this.amount = amount;
            this.label = label;
        }

        @Override
        public void proceed(NPC myself) {
//            EventBus.registerLabelHandler(label, this);
        }

        @Override
        public boolean freeToGo(NPC myself) {
            return label == null;
        }

        @Override
        public void close(String label, StoryAction<?> host) {
            amount--;
            if (amount == 0)
                this.label = null;
        }
        @Override
        public String toString() {
            return super.toString() + label + "#" + amount;
        }
    }
    public static class SetData extends NPCAction {
        private String value;
        private String key;
        public SetData(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void proceed(NPC myself) {
            DataStorage.setString(myself, key, value);

            if (key.equals("skin")) {
                myself.setSkin(value);
            } else if (key.equals("name")) {
                myself.setCustomName(value);
            }
        }

        @Override
        public boolean freeToGo(NPC myself) {
            return true;
        }
        @Override
        public String toString() {
            return super.toString() + value;
        }
    }
}
