package com.storyanvil.cogwheel.registry;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CogwheelBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CogwheelEngine.MODID);

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> r = BLOCKS.register(name, block);
        CogwheelItems.getITEMS().register(name, () -> new BlockItem(r.get(), new Item.Properties()));
        return r;
    }

    public static DeferredRegister<Block> getBLOCKS() {
        return BLOCKS;
    }
}
