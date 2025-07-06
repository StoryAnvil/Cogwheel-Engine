package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.infrustructure.StoryAction;

public interface LabelCloseable {
    void close(String label, StoryAction<?> host);
}
