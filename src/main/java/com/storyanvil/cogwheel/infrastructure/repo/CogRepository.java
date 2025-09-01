/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.infrastructure.repo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlags;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Consumer;

public class CogRepository implements RepositorySource {
    private PackType type;
    public CogRepository(PackType packType) {
        type = packType;
    }

    @Override
    public void loadPacks(@NotNull Consumer<Pack> load) {
        try {
            if (type == PackType.SERVER_DATA) {
//            Pack configLoader = Pack.create("cogwheel_server", Component.translatable("ui.storyanvil_cogwheel.server_pack"), true,
//                    this::serverResources, new Pack.Info(Component.empty(), 8, FeatureFlags.VANILLA_SET), PackType.SERVER_DATA, Pack.Position.TOP,
//                    true, new PackSource2());
                Pack configLoader = Pack.create("cogwheel_server", (Component) Component.translatable("ui.storyanvil_cogwheel.server_pack"), false,
                        this::serverResources, new Pack.Info(Component.empty(), 8, FeatureFlags.VANILLA_SET), PackType.SERVER_DATA, Pack.Position.TOP,
                        true, new PackSource2());
                load.accept(configLoader);
            }
        } catch (Exception e) {
            RuntimeException ee = new RuntimeException(e);
            ee.printStackTrace();
            throw ee;
        }
    }

    private PackResources serverResources(String id) {
        File pFile = new File(Minecraft.getInstance().gameDirectory, "config/cog-data");
        if (!pFile.exists()) pFile.mkdir();
        return new FilePackResources(id, pFile, true);
    }

    public static class PackSource2 implements PackSource {

        @Override
        public @NotNull Component decorate(@NotNull Component pName) {
            return pName;
        }

        @Override
        public boolean shouldAddAutomatically() {
            return true;
        }
    }
}
