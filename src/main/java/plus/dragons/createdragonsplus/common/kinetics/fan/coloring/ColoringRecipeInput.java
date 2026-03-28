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

package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * A simple container wrapper that also carries a {@link DyeColor} for coloring recipe matching.
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- replaces the RecipeInput record
 * since RecipeInput does not exist in 1.20.1. Uses SimpleContainer instead.
 */
public class ColoringRecipeInput extends SimpleContainer {
    private final DyeColor color;

    public ColoringRecipeInput(DyeColor color, ItemStack item) {
        super(item.copy());
        this.color = color;
    }

    public DyeColor color() {
        return color;
    }

    public ItemStack item() {
        return getItem(0);
    }
}
