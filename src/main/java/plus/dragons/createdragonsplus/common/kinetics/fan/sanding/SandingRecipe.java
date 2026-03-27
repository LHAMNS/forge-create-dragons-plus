/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.kinetics.fan.sanding;

import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;

public class SandingRecipe extends ProcessingRecipe<RecipeWrapper> {
    public SandingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDPRecipes.SANDING, params);
    }

    @Override
    protected int getMaxInputCount() { return 1; }

    @Override
    protected int getMaxOutputCount() { return 12; }

    @Override
    public boolean matches(RecipeWrapper inv, Level level) {
        if (inv.isEmpty()) return false;
        return ingredients.get(0).test(inv.getItem(0));
    }

    /**
     * Converts a SandPaperPolishingRecipe into a SandingRecipe for display in JEI.
     */
    public static SandingRecipe convertSandPaperPolishing(SandPaperPolishingRecipe original) {
        ResourceLocation id = new ResourceLocation(
                original.getId().getNamespace(),
                original.getId().getPath() + "_as_sanding");
        return new ProcessingRecipeBuilder<>(SandingRecipe::new, id)
                .withItemIngredients(original.getIngredients().get(0))
                .withItemOutputs(original.getRollableResults().get(0))
                .build();
    }
}
