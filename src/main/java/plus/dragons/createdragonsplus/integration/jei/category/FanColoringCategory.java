/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.item.ItemHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.data.internal.CDPLang;
import plus.dragons.createdragonsplus.integration.CompatUtility;
import plus.dragons.createdragonsplus.integration.ModIntegration;
import plus.dragons.createdragonsplus.integration.jei.CDPJeiPlugin;
import plus.dragons.createdragonsplus.integration.jei.widget.FanProcessingIcon;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;

public class FanColoringCategory extends ProcessingViaFanCategory<ColoringRecipe> {
    public static final mezz.jei.api.recipe.RecipeType<ColoringRecipe> TYPE =
            new mezz.jei.api.recipe.RecipeType<>(CDPRecipes.COLORING.getId(), ColoringRecipe.class);

    private FanColoringCategory(Info<ColoringRecipe> info) {
        super(info);
    }

    public static FanColoringCategory create() {
        var id = CDPCommon.asResource("fan_coloring");
        var title = CDPLang.description("recipe", id).component();
        var background = new EmptyBackground(178, 72);
        var icon = new Icon();
        var catalyst = AllBlocks.ENCASED_FAN.asStack();
        catalyst.setHoverName(CDPLang.description("recipe", id, "fan").component().withStyle(style -> style.withItalic(false)));
        var info = new Info<>(TYPE, title, background, icon, FanColoringCategory::getAllRecipes, CompatUtility.catalystWithIndustryFan(catalyst));
        return new FanColoringCategory(info);
    }

    @Override
    public void draw(ColoringRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        renderWidgets(graphics, recipe, mouseX, mouseY);

        PoseStack matrixStack = graphics.pose();

        matrixStack.pushPose();
        translateFan(matrixStack);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-12.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));

        AnimatedKinetics.defaultBlockElement(AllPartialModels.ENCASED_FAN_INNER)
                .rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
                .scale(SCALE)
                .render(graphics);

        AnimatedKinetics.defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
                .rotateBlock(0, 180, 0)
                .atLocal(0, 0, 0)
                .scale(SCALE)
                .render(graphics);

        renderAttachedBlock(graphics, recipe.getColor());
        matrixStack.popPose();
    }

    protected void renderAttachedBlock(GuiGraphics graphics, DyeColor color) {
        Fluid fluid = CDPFluids.DYES_BY_COLOR.get(color).getSource();
        GuiGameElement.of(fluid)
                .scale(SCALE)
                .atLocal(0, 0, 2)
                .lighting(AnimatedKinetics.DEFAULT_LIGHTING)
                .render(graphics);
    }

    /**
     * @deprecated use color-sensitive version instead.
     */
    @Override
    @Deprecated
    protected void renderAttachedBlock(GuiGraphics graphics) {}

    @SuppressWarnings("unchecked")
    private static List<ColoringRecipe> getAllRecipes() {
        var level = CDPJeiPlugin.getLevel();
        var manager = CDPJeiPlugin.getRecipeManager();
        var recipes = new ArrayList<>(manager.getAllRecipesFor((RecipeType<ColoringRecipe>) CDPRecipes.COLORING.getType()));
        // Load Create Garnished coloring recipes
        for (var color : DyeColors.ALL) {
            ResourceLocation recipeTypeId = ModIntegration.CREATE_GARNISHED.asResource(color.getSerializedName() + "_dye_blowing");
            RecipeType<?> garnishedType = BuiltInRegistries.RECIPE_TYPE.get(recipeTypeId);
            if (garnishedType == null) continue;
            @SuppressWarnings("rawtypes")
            var garnishedRecipes = manager.getAllRecipesFor((RecipeType) garnishedType);
            for (var recipe : garnishedRecipes) {
                if (recipe instanceof ProcessingRecipe<?> processing) {
                    ColoringRecipe coloring = new ProcessingRecipeBuilder<>(ColoringRecipe::new, processing.getId())
                            .withItemIngredients(processing.getIngredients().toArray(Ingredient[]::new))
                            .withItemOutputs(processing.getRollableResults().toArray(ProcessingOutput[]::new))
                            .build();
                    coloring.setColor(color);
                    recipes.add(coloring);
                }
            }
        }
        // Convert applicable crafting recipes
        for (var crafting : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (crafting.isSpecial())
                continue;
            var ingredients = crafting.getIngredients();
            var result = crafting.getResultItem(level.registryAccess());
            if (crafting.canCraftInDimensions(2, 1) && ingredients.size() == 2 && result.getCount() == 1) {
                for (var color : DyeColors.ALL) {
                    convert2x1(crafting.getId().withSuffix("_as_coloring"), color, ingredients, result).ifPresent(recipes::add);
                }
            } else if (crafting.canCraftInDimensions(3, 3) && ingredients.size() == 9 && result.getCount() == 8) {
                for (var color : DyeColors.ALL) {
                    convert3x3(crafting.getId().withSuffix("_as_coloring"), color, ingredients, result).ifPresent(recipes::add);
                }
            }
        }
        recipes.sort(Comparator
                .<ColoringRecipe, DyeColor>comparing(ColoringRecipe::getColor, DyeColors.creativeModeTabOrder())
                .thenComparing(r -> r.getId()));
        return recipes;
    }

    private static Optional<ColoringRecipe> convert2x1(ResourceLocation id, DyeColor color, List<Ingredient> ingredients, ItemStack result) {
        var dye = new ItemStack(DyeItem.byColor(color));
        int dyePos;
        if (ingredients.get(0).test(dye)) dyePos = 0;
        else if (ingredients.get(1).test(dye)) dyePos = 1;
        else return Optional.empty();
        var in = ingredients.get(dyePos == 0 ? 1 : 0);
        if (Arrays.stream(in.getItems()).anyMatch(i -> i.is(CDPItems.NOT_APPLICABLE_COLORING))) {
            var filtered = Arrays.stream(in.getItems()).filter(i -> !i.is(CDPItems.NOT_APPLICABLE_COLORING)).toArray(ItemStack[]::new);
            if (filtered.length == 0) return Optional.empty();
            var recipe = new ProcessingRecipeBuilder<>(ColoringRecipe::new, id)
                    .require(Ingredient.of(filtered))
                    .output(result.getItem())
                    .build();
            recipe.setColor(color);
            return Optional.of(recipe);
        } else {
            var recipe = new ProcessingRecipeBuilder<>(ColoringRecipe::new, id)
                    .require(in)
                    .output(result.getItem())
                    .build();
            recipe.setColor(color);
            return Optional.of(recipe);
        }
    }

    private static Optional<ColoringRecipe> convert3x3(ResourceLocation id, DyeColor color, List<Ingredient> ingredients, ItemStack result) {
        var dye = new ItemStack(DyeItem.byColor(color));
        Ingredient dyeable = null;
        boolean hasDye = false;
        for (var ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                return Optional.empty();
            } else if (ingredient.test(dye)) {
                if (hasDye)
                    return Optional.empty();
                hasDye = true;
            } else if (dyeable == null) {
                dyeable = ingredient;
            } else if (!ItemHelper.matchIngredients(dyeable, ingredient)) {
                return Optional.empty();
            }
        }
        if (!hasDye || dyeable == null)
            return Optional.empty();
        if (Arrays.stream(dyeable.getItems()).anyMatch(i -> i.is(CDPItems.NOT_APPLICABLE_COLORING))) {
            var filtered = Arrays.stream(dyeable.getItems()).filter(i -> !i.is(CDPItems.NOT_APPLICABLE_COLORING)).toArray(ItemStack[]::new);
            if (filtered.length == 0) return Optional.empty();
            ItemStack singleResult = result.copy();
            singleResult.setCount(1);
            var recipe = new ProcessingRecipeBuilder<>(ColoringRecipe::new, id)
                    .require(Ingredient.of(filtered))
                    .output(singleResult.getItem())
                    .build();
            recipe.setColor(color);
            return Optional.of(recipe);
        } else {
            ItemStack singleResult = result.copy();
            singleResult.setCount(1);
            var recipe = new ProcessingRecipeBuilder<>(ColoringRecipe::new, id)
                    .require(dyeable)
                    .output(singleResult.getItem())
                    .build();
            recipe.setColor(color);
            return Optional.of(recipe);
        }
    }

    @FieldsNullabilityUnknownByDefault
    protected static class Icon extends FanProcessingIcon {
        private ItemStack[] catalystStacks;

        @Override
        protected ItemStack getCatalyst() {
            if (catalystStacks == null) {
                catalystStacks = Arrays.stream(DyeColors.ALL)
                        .map(CDPFluids.DYES_BY_COLOR::get)
                        .map(entry -> new ItemStack(entry.getBucket().get()))
                        .toArray(ItemStack[]::new);
            }
            return catalystStacks[(AnimationTickHolder.getTicks() / 20) % catalystStacks.length];
        }
    }
}
