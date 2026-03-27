/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/**
 * A simple equivalent of NeoForge's {@code RecipeHolder<T>} for Forge 1.20.1.
 * In NeoForge 1.21+, recipes are wrapped in RecipeHolder to carry their id alongside
 * the recipe instance. In Forge 1.20.1, recipes carry their own id, but we provide
 * this wrapper to keep the API surface consistent with upstream.
 *
 * @param id    the recipe's resource location
 * @param value the recipe instance
 * @param <T>   the recipe type
 */
public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
}
