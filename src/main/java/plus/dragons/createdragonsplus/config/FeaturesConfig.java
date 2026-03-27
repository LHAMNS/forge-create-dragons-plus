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

package plus.dragons.createdragonsplus.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base config class for feature toggles. Each {@link ConfigFeature} is both a config boolean
 * and a Forge recipe condition (via {@link ConfigFeatureCondition}), allowing features to be
 * toggled in config and also used as recipe load conditions.
 * Ported from NeoForge 1.21.1 to Forge 1.20.1 -- uses Forge's ICondition (via
 * ConfigFeatureCondition serializer) instead of NeoForge's ICondition with MapCodec.
 * Uses catnip ConfigBase for config registration.
 */
public class FeaturesConfig extends ConfigBase {
    private static final ConcurrentHashMap<ResourceLocation, ConfigFeature> FEATURES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ConfigFeature> FEATURES_VIEW = Collections.unmodifiableMap(FEATURES);
    protected final String modid;

    public FeaturesConfig(String modid) {
        this.modid = modid;
    }

    public static @UnmodifiableView Map<ResourceLocation, ConfigFeature> getFeatures() {
        return FEATURES_VIEW;
    }

    public static boolean isFeatureEnabled(ResourceLocation id) {
        return FEATURES.containsKey(id) && FEATURES.get(id).get();
    }

    protected @Nullable Boolean getFeatureOverride(ResourceLocation id) {
        String key = id.toString();
        Boolean override = null;
        for (var mod : ModList.get().getMods()) {
            var properties = mod.getModProperties();
            if (properties.get(key) instanceof Boolean flag) {
                override = flag;
            }
        }
        return override;
    }

    protected ConfigFeature feature(boolean enabled, String name, String... comment) {
        return new ConfigFeature(name, enabled, comment);
    }

    /**
     * A config boolean that also serves as a feature toggle. When used as a recipe condition,
     * it delegates to {@link ConfigFeatureCondition} which is registered with Forge's
     * crafting condition system.
     */
    public class ConfigFeature extends ConfigBool {
        private final ResourceLocation id;
        private final @Nullable Boolean override;

        public ConfigFeature(String name, boolean def, String... comment) {
            super(name, def, comment);
            this.id = new ResourceLocation(modid, name);
            this.override = getFeatureOverride(this.id);
            if (FEATURES.containsKey(id))
                throw new IllegalStateException("Config features with id [" + id + "] already registered");
            FEATURES.put(id, this);
            // Also register with the ConfigFeatureCondition system
            ConfigFeatureCondition.registerFeature(id.toString(), this::get);
        }

        public ResourceLocation getId() {
            return id;
        }

        public ConfigFeature addAlias(String name) {
            ResourceLocation aliasId = new ResourceLocation(modid, name);
            FEATURES.put(aliasId, this);
            ConfigFeatureCondition.registerFeature(aliasId.toString(), this::get);
            return this;
        }

        @Override
        public Boolean get() {
            return override == null ? super.get() : override;
        }

        /**
         * Creates a Forge recipe condition for this feature.
         */
        public ICondition asCondition() {
            return new ConfigFeatureCondition(id.toString());
        }
    }

    @Override
    public String getName() {
        return "features";
    }
}
