/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.integration;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModList;
import plus.dragons.createdragonsplus.util.DeferredHolder;

public enum ModIntegration {
    CREATE_GARNISHED(Constants.CREATE_GARNISHED),
    CREATE_DND(Constants.CREATE_DND),
    QUICKSAND(Constants.QUICKSAND);

    private final String id;

    ModIntegration(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean enabled() {
        return ModList.get().isLoaded(id);
    }

    public ResourceLocation asResource(String path) {
        return new ResourceLocation(id, path);
    }

    public void onConstructMod() {}

    public void onCommonSetup() {}

    public void onClientSetup() {}

    public static class Constants {
        public static final String CREATE_GARNISHED = "garnished";
        public static final String CREATE_DND = "create_dd";
        public static final String QUICKSAND = "quicksand";
    }

    /**
     * Returns a {@link DeferredHolder} for a {@link FanProcessingType} from Create's fan type registry.
     * <p>
     * Matches upstream NeoForge signature: {@code DeferredHolder<FanProcessingType, FanProcessingType>}.
     */
    public DeferredHolder<FanProcessingType, FanProcessingType> fanType(String path) {
        return DeferredHolder.create(CreateRegistries.FAN_PROCESSING_TYPE, asResource(path));
    }

    /**
     * Returns a {@link DeferredHolder} for a {@link RecipeType} from the built-in recipe type registry.
     * <p>
     * In upstream NeoForge this returns
     * {@code DeferredHolder<RecipeType<?>, RecipeType<StandardProcessingRecipe<SingleRecipeInput>>>}.
     * On Forge 1.20.1, the inner type simplifies to {@code RecipeType<?>} since
     * StandardProcessingRecipe and SingleRecipeInput are not available.
     */
    @SuppressWarnings("unchecked")
    public DeferredHolder<RecipeType<?>, RecipeType<?>> recipeType(String path) {
        return DeferredHolder.create(Registries.RECIPE_TYPE, asResource(path));
    }
}
