package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface ScriptLineHandler {
    /**
     * Method for handler script's line
     * @param line line that needs to be executed
     * @param script script executing line
     * @return DoubleValue provided by one of static ScriptLineHandler methods
     */
    @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @NotNull DispatchedScript script) throws Exception;

    /**
     * @return resource location for this ScriptLineHandler. Return value of this method must be constant!
     */
    @NotNull ResourceLocation getResourceLocation();

    DoubleValue<Boolean, Boolean> ignore = new DoubleValue<>(false, true);
    DoubleValue<Boolean, Boolean> continueReading = new DoubleValue<>(true, true);
    DoubleValue<Boolean, Boolean> blocking = new DoubleValue<>(true, false);

    /**
     * Use this method if your ScriptLineHandler is not applicable for provided line of CogScript code
     * @return DoubleValue for returning in ScriptLineHandler#handle.
     */
    static DoubleValue<Boolean, Boolean> ignore() {
        return ignore;
    }
    /**
     * Use this method if your ScriptLineHandler is applicable for provided line of CogScript code and line was in fact handled so dispatched script can continue executing lines
     * @return DoubleValue for returning in ScriptLineHandler#handle.
     */
    static DoubleValue<Boolean, Boolean> continueReading() {
        return continueReading;
    }
    /**
     * Use this method if your ScriptLineHandler is applicable for provided line of CogScript code and line was in fact handled but dispatched script shall not continue line execution.
     * <br>If you are using this make sure to schedule DispatchedScript#lineDispatcher somehow so script will be able to continue execution
     * @return DoubleValue for returning in ScriptLineHandler#handle.
     */
    static DoubleValue<Boolean, Boolean> blocking() {
        return blocking;
    }
}
