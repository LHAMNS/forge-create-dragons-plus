/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.kinetics.fan.ending;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.ModIntegration;
import plus.dragons.createdragonsplus.util.DeferredHolder;

import java.util.List;
import java.util.Optional;

/**
 * Ending fan processing type. Implements {@link FanProcessingType} directly,
 * matching the upstream NeoForge structure. Uses {@link DeferredHolder} for
 * Create DnD compat lookups.
 */
public class EndingFanProcessingType implements FanProcessingType {
    private final RecipeWrapper reusableWrapper = new RecipeWrapper(new net.minecraftforge.items.ItemStackHandler(1));

    private final DeferredHolder<FanProcessingType, FanProcessingType> createDNDType;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> createDNDRecipe;

    public EndingFanProcessingType() {
        this.createDNDType = ModIntegration.CREATE_DND.fanType("dragon_breathing");
        this.createDNDRecipe = ModIntegration.CREATE_DND.recipeType("dragon_breathing");
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!CDPConfig.server().enableBulkEnding.get()) return false;
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.is(CDPFluids.MOD_TAGS.fanEndingCatalysts)) return true;
        BlockState state = level.getBlockState(pos);
        if (state.is(CDPBlocks.FAN_ENDING_CATALYSTS)) return true;
        return createDNDType.isBound() && createDNDType.get().isValidAt(level, pos);
    }

    @Override
    public int getPriority() {
        return 350; // Greater than Haunting, smaller than Washing
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        if (!CDPConfig.server().enableBulkEnding.get()) return false;
        reusableWrapper.setItem(0, stack);
        if (level.getRecipeManager()
                .getRecipeFor(CDPRecipes.ENDING.getType(), reusableWrapper, level)
                .isPresent()) return true;
        return canProcessByCompatRecipe(createDNDRecipe, stack, level);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        reusableWrapper.setItem(0, stack);
        var recipe = level.getRecipeManager()
                .getRecipeFor(CDPRecipes.ENDING.getType(), reusableWrapper, level);
        if (recipe.isPresent()) {
            return RecipeApplier.applyRecipeOn(level, stack, recipe.get(), true);
        }
        Optional<List<ItemStack>> dndResult = processByCompatRecipe(createDNDRecipe, stack, level);
        return dndResult.orElse(null);
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.DRAGON_BREATH,
                    pos.x + (level.random.nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.random.nextFloat() - .5f) * .5f,
                    0, 1 / 8f, 0);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess access, RandomSource random) {
        int color = Color.mixColors(0xB700D2, 0xDF00F9, random.nextFloat());
        access.setColor(color);
        access.setAlpha(1f);
        if (random.nextFloat() < 1 / 32f)
            access.spawnExtraParticle(ParticleTypes.DRAGON_BREATH, 0f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide) return;
        if (entity instanceof LivingEntity living
                && living.isAffectedByPotions()
                && entity.tickCount % 5 == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
        }
        if (createDNDType.isBound()) {
            createDNDType.get().affectEntity(entity, level);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean canProcessByCompatRecipe(DeferredHolder<RecipeType<?>, RecipeType<?>> recipeType,
            ItemStack stack, Level level) {
        if (!recipeType.isBound()) return false;
        reusableWrapper.setItem(0, stack);
        return level.getRecipeManager()
                .getRecipeFor((RecipeType) recipeType.get(), reusableWrapper, level)
                .isPresent();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Optional<List<ItemStack>> processByCompatRecipe(DeferredHolder<RecipeType<?>, RecipeType<?>> recipeType,
            ItemStack stack, Level level) {
        if (!recipeType.isBound()) return Optional.empty();
        reusableWrapper.setItem(0, stack);
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> opt =
                level.getRecipeManager().getRecipeFor((RecipeType) recipeType.get(), reusableWrapper, level);
        if (opt.isEmpty()) return Optional.empty();
        return Optional.of(RecipeApplier.applyRecipeOn(level, stack, opt.get(), true));
    }
}
