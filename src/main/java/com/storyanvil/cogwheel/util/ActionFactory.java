package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.infrustructure.StoryAction;

public interface ActionFactory {
    StoryAction<?> construct(String[] tag) throws RuntimeException;
}
