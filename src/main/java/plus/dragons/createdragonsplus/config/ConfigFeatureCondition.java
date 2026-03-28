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

package plus.dragons.createdragonsplus.config;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import plus.dragons.createdragonsplus.common.CDPCommon;

/**
 * A recipe condition that checks a boolean config value at load time.
 * Equivalent to NeoForge's ConfigFeature ICondition.
 */
public class ConfigFeatureCondition implements ICondition {
    private static final ResourceLocation ID = new ResourceLocation(CDPCommon.ID, "config_feature");
    private static final Map<String, Supplier<Boolean>> FEATURES = new ConcurrentHashMap<>();

    private final String featureName;

    public ConfigFeatureCondition(String featureName) {
        this.featureName = featureName;
    }

    public static void registerFeature(String name, Supplier<Boolean> supplier) {
        FEATURES.put(name, supplier);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        Supplier<Boolean> supplier = FEATURES.get(featureName);
        // If not found and the key contains a namespace prefix (e.g. "create_dragons_plus:block/fluid_hatch"),
        // try stripping the namespace and looking up just the path part (e.g. "block/fluid_hatch").
        if (supplier == null && featureName.contains(":")) {
            String stripped = featureName.substring(featureName.indexOf(':') + 1);
            supplier = FEATURES.get(stripped);
        }
        return supplier != null && supplier.get();
    }

    public static class Serializer implements IConditionSerializer<ConfigFeatureCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ConfigFeatureCondition value) {
            json.addProperty("feature", value.featureName);
        }

        @Override
        public ConfigFeatureCondition read(JsonObject json) {
            return new ConfigFeatureCondition(json.get("feature").getAsString());
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
