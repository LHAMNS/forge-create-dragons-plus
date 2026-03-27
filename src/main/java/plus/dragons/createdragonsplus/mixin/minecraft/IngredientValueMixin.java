/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.mixin.minecraft;

/**
 * STUB - Not applicable for Forge 1.20.1.
 * <p>
 * In the NeoForge 1.21.1 version, this mixin modifies the {@code Ingredient.Value} codec
 * to support {@code IntegrationIngredient.Value} during datagen. In Forge 1.20.1,
 * {@code Ingredient.Value} uses JSON serialization ({@code serialize()}/{@code fromJson()})
 * rather than {@code MapCodec}, so this mixin has no target to modify.
 * <p>
 * Additionally, the conditional mixin library ({@code me.fallenbreath.conditionalmixin})
 * used to restrict this to datagen-only is not available for Forge 1.20.1.
 * <p>
 * If IntegrationIngredient support is needed in the Forge port, it should be implemented
 * by overriding the JSON serialization path instead.
 */
public class IngredientValueMixin {
    // Intentionally empty - see class Javadoc for explanation.
}
