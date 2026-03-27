/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;

public class ColoringRecipe extends ProcessingRecipe<RecipeWrapper> {
    private DyeColor color = DyeColor.WHITE;

    public ColoringRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDPRecipes.COLORING, params);
    }

    public DyeColor getColor() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level level) {
        if (inv.isEmpty()) return false;
        return ingredients.get(0).test(inv.getItem(0));
    }

    @Override
    public void readAdditional(JsonObject json) {
        super.readAdditional(json);
        if (json.has("color")) {
            this.color = DyeColor.byName(GsonHelper.getAsString(json, "color"), DyeColor.WHITE);
        }
    }

    @Override
    public void writeAdditional(JsonObject json) {
        super.writeAdditional(json);
        json.addProperty("color", this.color.getSerializedName());
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        super.readAdditional(buffer);
        this.color = buffer.readEnum(DyeColor.class);
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        super.writeAdditional(buffer);
        buffer.writeEnum(this.color);
    }

    /**
     * Creates a new ColoringRecipe via ProcessingRecipeBuilder and sets the color.
     * The returned recipe has the specified color set.
     */
    public static ColoringRecipe buildColoring(ResourceLocation id, DyeColor color,
                                                java.util.function.Consumer<ProcessingRecipeBuilder<ColoringRecipe>> configure) {
        ProcessingRecipeBuilder<ColoringRecipe> builder = new ProcessingRecipeBuilder<>(ColoringRecipe::new, id);
        configure.accept(builder);
        ColoringRecipe recipe = builder.build();
        recipe.setColor(color);
        return recipe;
    }
}
