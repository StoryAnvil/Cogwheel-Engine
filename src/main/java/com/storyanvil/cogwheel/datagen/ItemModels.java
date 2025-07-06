package com.storyanvil.cogwheel.datagen;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemModels extends ItemModelProvider {
    public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CogwheelEngine.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(modid, "item/" + item.getId().getPath()));
    }
    private void bruh(RegistryObject<? extends Block> block) {
        this.withExistingParent(CogwheelEngine.MODID + ":" + block.getId().getPath(),
                modLoc("block/" + block.getId().getPath()));
    }
}
