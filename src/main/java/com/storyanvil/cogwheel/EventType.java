package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;

import java.util.HashMap;

public enum EventType {
    BLOCK_PLACED, BLOCK_BROKEN, PLAYER_ATE;

    private static final HashMap<EventType, String> SUBSCRIBERS = new HashMap<>();
    public static void dispatchEvent(EventType type) {
        if (!SUBSCRIBERS.containsKey(type)) return;
        CogScriptDispatcher.dispatch(SUBSCRIBERS.get(type));
    }
    public static void dispatchEvent(EventType type, HashMap<String, CogPropertyManager> storage) {
        if (!SUBSCRIBERS.containsKey(type)) return;
        CogScriptDispatcher.dispatch(SUBSCRIBERS.get(type), storage);
    }
    public static void setSubscriber(EventType type, String script) {
        SUBSCRIBERS.put(type, script);
    }
}
