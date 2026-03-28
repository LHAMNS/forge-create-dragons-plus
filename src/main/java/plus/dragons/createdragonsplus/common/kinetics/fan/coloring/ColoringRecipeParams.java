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

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

/**
 * Extended processing recipe params that includes a {@link DyeColor} for coloring recipes.
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- uses FriendlyByteBuf instead of
 * RegistryFriendlyByteBuf, and drops MapCodec/StreamCodec infrastructure that doesn't
 * exist in 1.20.1. The color is serialized via network buffer manually.
 */
public class ColoringRecipeParams extends ProcessingRecipeBuilder.ProcessingRecipeParams {
    protected DyeColor color;

    public ColoringRecipeParams(ResourceLocation id) {
        super(id);
        this.color = DyeColor.WHITE;
    }

    public ColoringRecipeParams(ResourceLocation id, DyeColor color) {
        super(id);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }

    public ColoringRecipeParams setColor(DyeColor color) {
        this.color = color;
        return this;
    }

    public void writeExtra(FriendlyByteBuf buffer) {
        buffer.writeEnum(color);
    }

    public void readExtra(FriendlyByteBuf buffer) {
        this.color = buffer.readEnum(DyeColor.class);
    }
}
