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

package plus.dragons.createdragonsplus.integration.jei;

import com.google.common.base.Preconditions;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.jei.category.FanColoringCategory;
import plus.dragons.createdragonsplus.integration.jei.category.FanEndingCategory;
import plus.dragons.createdragonsplus.integration.jei.category.FanFreezingCategory;
import plus.dragons.createdragonsplus.integration.jei.category.FanSandingCategory;
import plus.dragons.createdragonsplus.util.ErrorMessages;

@JeiPlugin
public class CDPJeiPlugin implements IModPlugin {
    public static final ResourceLocation ID = CDPCommon.asResource("jei_plugin");
    private final List<CreateRecipeCategory<?>> categories = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        this.categories.clear();
        if (CDPConfig.server().enableBulkColoring.get())
            this.categories.add(FanColoringCategory.create());
        if (CDPConfig.server().enableBulkFreezing.get())
            this.categories.add(FanFreezingCategory.create());
        if (CDPConfig.server().enableBulkSanding.get())
            this.categories.add(FanSandingCategory.create());
        if (CDPConfig.server().enableBulkEnding.get())
            this.categories.add(FanEndingCategory.create());
        registration.addRecipeCategories(categories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        categories.forEach(category -> category.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        categories.forEach(category -> category.registerCatalysts(registration));
    }

    public static Level getLevel() {
        if (FMLEnvironment.dist != Dist.CLIENT)
            throw new IllegalStateException("Retrieving client level is only supported for client");
        var minecraft = Minecraft.getInstance();
        Preconditions.checkNotNull(minecraft, ErrorMessages.notNull("minecraft"));
        var level = minecraft.level;
        Preconditions.checkNotNull(level, ErrorMessages.notNull("level"));
        return level;
    }

    public static RecipeManager getRecipeManager() {
        if (FMLEnvironment.dist != Dist.CLIENT)
            throw new IllegalStateException("Retrieving recipe manager from client level is only supported for client");
        var minecraft = Minecraft.getInstance();
        Preconditions.checkNotNull(minecraft, ErrorMessages.notNull("minecraft"));
        var level = minecraft.level;
        Preconditions.checkNotNull(level, ErrorMessages.notNull("level"));
        return level.getRecipeManager();
    }
}
