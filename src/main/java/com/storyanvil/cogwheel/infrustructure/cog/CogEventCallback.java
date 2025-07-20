package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

public class CogEventCallback implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("event", CogEventCallback::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("setCanceled", (name, args, script, o) -> {
            CogEventCallback callback = (CogEventCallback) o;
            callback.canceled = args.requireBoolean(0);
            return callback;
        });
    }

    private boolean canceled = false;

    public CogEventCallback() {
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return true; // Props need to be checked dynamically
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        if (MANAGER.hasOwnProperty(name))
            return MANAGER.get(name).handle(name, args, script, this);
        String key = "event_" + name; // do not access event variables directly. Use Cogwheel.getEvent().VariableName() instead
        if (script.hasKey(key))
            return script.get(key);
        throw new RuntimeException("Property " + name + " does not exist in CogEventCallback");
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return false;
    }

    public boolean isCanceled() {
        return canceled;
    }
}
