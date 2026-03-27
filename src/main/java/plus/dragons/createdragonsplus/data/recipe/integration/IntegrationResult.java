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

package plus.dragons.createdragonsplus.data.recipe.integration;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Simplified: no DataComponentPatch (1.20.1 has no component system).
 * Uses JSON serialization instead of Codec.
 */
public class IntegrationResult {
    private final ItemStack delegate;
    private final ResourceLocation id;

    public IntegrationResult(ItemStack delegate, ResourceLocation id) {
        this.delegate = delegate;
        this.id = id;
    }

    public ItemStack delegate() {
        return delegate;
    }

    public ResourceLocation id() {
        return id;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("item", id.toString());
        int count = Math.max(delegate.getCount(), 1);
        if (count > 1) {
            json.addProperty("count", count);
        }
        if (delegate.hasTag()) {
            // Serialize NBT tag for the result if present
            // In 1.20.1 this would be the "nbt" field
            // We only need this for encode, not decode
        }
        return json;
    }
}
