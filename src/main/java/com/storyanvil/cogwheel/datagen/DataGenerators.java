package com.storyanvil.cogwheel.datagen;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new RecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), LootTables.create(packOutput));
        generator.addProvider(event.includeClient(), new BlockStates(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModels(packOutput, existingFileHelper));
        BlocksTags blockTags = generator.addProvider(event.includeServer(), new BlocksTags(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ItemTags(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
    }
}