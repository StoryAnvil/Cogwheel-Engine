package com.storyanvil.cogwheel.datagen;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemTags extends ItemTagsProvider {
    public ItemTags(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, CogwheelEngine.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
    }

    public static TagKey<Item> t(String namespace, String path) {
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
    public static TagKey<Item> t(String path) {
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, path));
    }
    public static TagKey<Item> f(String path) {
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath("forge", path));
    }
}
