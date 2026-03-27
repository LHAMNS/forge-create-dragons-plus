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

package plus.dragons.createdragonsplus.data.internal;

import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createdragonsplus.common.CDPCommon;

/**
 * Language helper for Create Dragons Plus.
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 */
public class CDPLang {
    public static LangBuilder builder() {
        return new LangBuilder(CDPCommon.ID);
    }

    public static LangBuilder number(double d) {
        return builder().text(LangNumberFormat.format(d));
    }

    /**
     * Creates a translatable description component.
     * Example: description("recipe", rl) -> "create_dragons_plus.recipe.create_dragons_plus.fan_coloring"
     */
    public static LangBuilder description(String type, ResourceLocation id) {
        return builder().translate(CDPCommon.ID + "." + type + "." + id.getNamespace() + "." + id.getPath());
    }

    public static LangBuilder description(String type, ResourceLocation id, String suffix) {
        return builder().translate(CDPCommon.ID + "." + type + "." + id.getNamespace() + "." + id.getPath() + "." + suffix);
    }
}
