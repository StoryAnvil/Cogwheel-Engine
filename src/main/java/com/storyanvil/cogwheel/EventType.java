package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public enum EventType {
    BLOCK_PLACED, BLOCK_BROKEN, PLAYER_ATE,
    CHAT_MESSAGE, TOTEM_USAGE, BLOCK_RIGHT_CLICK,
    ENTITY_RIGHT_CLICK, PLAYER_RESPAWN, ENTITY_ATTACKED,
    BELT_MESSAGE, INITIALIZE;

    private static final HashMap<EventType, String> SUBSCRIBERS = new HashMap<>();
    public static void dispatchEvent(EventType type) {
        if (!SUBSCRIBERS.containsKey(type)) return;
        CogScriptDispatcher.dispatch(SUBSCRIBERS.get(type));
    }
    public static void dispatchEvent(EventType type, HashMap<String, CogPropertyManager> storage) {
        if (!SUBSCRIBERS.containsKey(type)) return;
        CogScriptDispatcher.dispatch(SUBSCRIBERS.get(type), storage);
    }
    public static void setSubscriber(EventType type, @NotNull String script) {
        if (script.equals("unset")) {
            SUBSCRIBERS.remove(type);
            return;
        }
        SUBSCRIBERS.put(type, script);
    }
}
