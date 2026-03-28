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

package plus.dragons.createdragonsplus.data.recipe;

import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Uses Forge 1.20.1 recipe builders with FinishedRecipe pattern.
 */
public class VanillaRecipeBuilders {
    public static ShapedRecipeBuilder shaped() {
        return new ShapedRecipeBuilder("crafting");
    }

    public static ShapelessRecipeBuilder shapeless() {
        return new ShapelessRecipeBuilder("crafting");
    }

    public static SingleItemRecipeBuilder stonecutting() {
        return new SingleItemRecipeBuilder("stonecutting");
    }

    public static CookingRecipeBuilder smelting() {
        return new CookingRecipeBuilder("smelting", "smelting", 200);
    }

    public static CookingRecipeBuilder blasting() {
        return new CookingRecipeBuilder("blasting", "blasting", 100);
    }

    public static CookingRecipeBuilder smoking() {
        return new CookingRecipeBuilder("smoking", "smoking", 100);
    }

    public static CookingRecipeBuilder campfire() {
        return new CookingRecipeBuilder("campfire_cooking", "campfire_cooking", 600);
    }
}
