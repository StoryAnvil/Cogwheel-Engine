package com.storyanvil.cogwheel.datagen;
import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class BlockStates extends BlockStateProvider {
    public BlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, CogwheelEngine.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }

    public ResourceLocation r(String path) {
        return ResourceLocation.fromNamespaceAndPath("storyanvil", "block/" + path);
    }
    public String name(Block block) {
        return key(block).getPath();
    }
    private ResourceLocation key(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    private void blockWithItem(@NotNull RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
}