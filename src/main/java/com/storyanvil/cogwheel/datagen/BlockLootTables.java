package com.storyanvil.cogwheel.datagen;
import com.storyanvil.cogwheel.registry.CogwheelBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockLootTables extends BlockLootSubProvider {
    protected BlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return CogwheelBlocks.getBLOCKS().getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
