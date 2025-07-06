/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.datagen;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {
    public RecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    private Consumer<FinishedRecipe> writer;
    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        this.writer = writer;
        this.writer = null;
    }

    @Contract("_ -> new")
    private @NotNull ResourceLocation r(String path) {
        return ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, path);
    }
    private @NotNull ShapedRecipeBuilder shaped(@NotNull RegistryObject<Item> item) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item.get(), 1).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapedRecipeBuilder shaped(@NotNull RegistryObject<Item> item, int amount) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item.get(), amount).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapedRecipeBuilder shaped(ItemLike item) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item, 1).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapedRecipeBuilder shaped(ItemLike item, int amount) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item, amount).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapelessRecipeBuilder shapeless(ItemLike item) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, 1).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapelessRecipeBuilder shapeless(ItemLike item, int amount) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, amount).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapelessRecipeBuilder shapeless(@NotNull RegistryObject<Item> item) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item.get(), 1).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private @NotNull ShapelessRecipeBuilder shapeless(@NotNull RegistryObject<Item> item, int amount) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item.get(), amount).unlockedBy("i", new ImpossibleTrigger.TriggerInstance());
    }
    private void smelting(Ingredient in, ItemLike out, int cookingTime, ResourceLocation r) {
        SimpleCookingRecipeBuilder.smelting(
                in, RecipeCategory.MISC, out, 0, cookingTime
        ).unlockedBy("impossible", new ImpossibleTrigger.TriggerInstance()).save(writer, r);
    }
    private void compacting(ItemLike small, ItemLike big) {
        String si = ForgeRegistries.ITEMS.getKey(small.asItem()).getPath();
        String bi = ForgeRegistries.ITEMS.getKey(big.asItem()).getPath();
        shapeless(big)
                .requires(small, 9)
                .save(writer, r("compact/" + si + "_" + bi));
        shapeless(small, 9)
                .requires(big)
                .save(writer, r("compact/" + bi + "_" + si));
    }
}
