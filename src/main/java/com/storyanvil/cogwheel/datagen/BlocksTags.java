package com.storyanvil.cogwheel.datagen;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BlocksTags extends BlockTagsProvider {
    public BlocksTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CogwheelEngine.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
    }

    public static TagKey<Block> t(String namespace, String path) {
        return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
    public static TagKey<Block> t(String path) {
        return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, path));
    }
}