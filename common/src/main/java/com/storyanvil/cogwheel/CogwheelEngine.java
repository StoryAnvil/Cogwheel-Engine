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

package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.config.CogwheelClientConfig;
import com.storyanvil.cogwheel.infrastructure.cog.PropertyHandler;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class CogwheelEngine {
    public static final String MODID = "storyanvil_cogwheel";
    public static final Logger LOGGER = LoggerFactory.getLogger("STORYANVIL/COGWHEEL");
    public static final Logger EARLY = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/EARLY-LOGGER");
    public static HashMap<String, PropertyHandler> EARLY_MANAGER = new HashMap<>();

    public static void init() {
        setupLogInterceptor();
        CogwheelClientConfig.reload();
        CogwheelRegistries.registerDefaultObjects();
    }

    private static void setupLogInterceptor() {
        org.apache.logging.log4j.Logger rootLogger = LogManager.getRootLogger();
        AbstractFilter filter = new AbstractFilter() {
            public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("STORYANVIL/COGWHEEL/INTERCEPTOR");
//            private boolean filterInternal(Level level, org.apache.logging.log4j.core.Logger logger) {
//                return CogwheelClientConfig.isShowingAllLogs() && !logger.equals(LOGGER) && (level.equals(Level.DEBUG) || level.equals(Level.TRACE));
//            }

            @Override
            public Result filter(LogEvent event) {
                if (CogwheelClientConfig.isLoggerIgnored(event.getLoggerName())) {
                    return Result.DENY;
                }
                if (CogwheelClientConfig.isShowingAllLogs() && !event.getLoggerName().equals("STORYANVIL/COGWHEEL/INTERCEPTOR") && (event.getLevel().equals(Level.DEBUG) || (event.getLevel().equals(Level.TRACE) && CogwheelClientConfig.isTraceLogLevelEnabled()))) {
                    String markers = "/";
                    if (event.getMarker() != null) {
                        if (event.getMarker().hasParents()) {
                            StringBuilder m = new StringBuilder();
                            markerStep(m, event.getMarker());
                        } else {
                            markers = event.getMarker().getName() + '/';
                        }
                    }
                    if (event.getThrown() == null)
                        LOGGER.info("{}@{} ({})\n        {}", event.getLevel(), event.getLoggerName(), markers, event.getMessage().getFormattedMessage());
                    else
                        LOGGER.info("{}@{} ({})\n        {}", event.getLevel(), event.getLoggerName(), markers, event.getMessage().getFormattedMessage(), event.getThrown());
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }

            private void markerStep(StringBuilder sb, Marker marker) {
                sb.append(marker.getName()).append('/');
                if (marker.hasParents()) {
                    for (Marker parent : marker.getParents()) {
                        markerStep(sb, parent);
                    }
                }
            }

            /*@Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Message msg, Throwable t) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, t);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Object msg, Throwable t) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, t);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object... params) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, params);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3, p4);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3, p4, p5);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3, p4, p5, p6);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3, p4, p5, p6, p7);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3, p4, p5, p6, p7, p8);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
                if (filterInternal(level, logger)) {
                    LOGGER.info(marker, msg, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
                    return Result.DENY;
                }
                return Result.ACCEPT;
            }*/
        };
        ((org.apache.logging.log4j.core.Logger) rootLogger).addFilter(filter);
        CogwheelHooks.startupMessage("CE's Log Interceptor registered!");
    }
}
