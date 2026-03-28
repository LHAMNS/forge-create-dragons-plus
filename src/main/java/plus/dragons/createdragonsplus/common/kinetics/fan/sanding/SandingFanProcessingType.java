/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.kinetics.fan.sanding;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.kinetics.fan.DynamicParticleFanProcessingType;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.ModIntegration;
import plus.dragons.createdragonsplus.util.DeferredHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Sanding fan processing type. Implements {@link DynamicParticleFanProcessingType} directly,
 * matching the upstream NeoForge structure. Uses {@link DeferredHolder} for
 * Create DnD compat lookups, and includes sandpaper polishing fallback.
 */
public class SandingFanProcessingType implements DynamicParticleFanProcessingType<SandingFanProcessingType.ParticleData> {
    private final RecipeWrapper reusableWrapper = new RecipeWrapper(new net.minecraftforge.items.ItemStackHandler(1));

    /**
     * Default sandy tint color matching the original: 0xDBD3A0 = RGB(219, 211, 160).
     */
    private static final int SANDING_COLOR = 0xDBD3A0;

    private final DeferredHolder<FanProcessingType, FanProcessingType> createDNDType;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> createDNDRecipe;

    public SandingFanProcessingType() {
        this.createDNDType = ModIntegration.CREATE_DND.fanType("sanding");
        this.createDNDRecipe = ModIntegration.CREATE_DND.recipeType("sanding");
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!CDPConfig.server().enableBulkSanding.get()) return false;
        BlockState state = level.getBlockState(pos);
        if (state.is(CDPBlocks.FAN_SANDING_CATALYSTS)) return true;
        return createDNDType.isBound() && createDNDType.get().isValidAt(level, pos);
    }

    @Override
    public int getPriority() {
        return 700; // Should be greater than Bulk Freezing
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        if (!CDPConfig.server().enableBulkSanding.get()) return false;
        reusableWrapper.setItem(0, stack);
        var recipe = level.getRecipeManager()
                .getRecipeFor(CDPRecipes.SANDING.getType(), reusableWrapper, level);
        if (recipe.isPresent()) return true;
        if (AllRecipeTypes.SANDPAPER_POLISHING.find(reusableWrapper, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED)
                .isPresent()) return true;
        return canProcessByCompatRecipe(createDNDRecipe, stack, level);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        reusableWrapper.setItem(0, stack);
        var recipe = level.getRecipeManager()
                .getRecipeFor(CDPRecipes.SANDING.getType(), reusableWrapper, level);
        if (recipe.isPresent()) {
            return RecipeApplier.applyRecipeOn(level, stack, recipe.get(), true);
        }
        var polishingRecipe = AllRecipeTypes.SANDPAPER_POLISHING.find(reusableWrapper, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (polishingRecipe.isPresent()) {
            return RecipeApplier.applyRecipeOn(level, stack, polishingRecipe.get(), true);
        }
        Optional<List<ItemStack>> dndResult = processByCompatRecipe(createDNDRecipe, stack, level);
        return dndResult.orElse(null);
    }

    @Override
    @Nullable
    public ParticleData getParticleDataAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        int color = SANDING_COLOR;
        if (state.getBlock() instanceof FallingBlock falling)
            color = falling.getDustColor(state, level, pos);
        return new ParticleData(state, color);
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos, @Nullable ParticleData data) {
        if (level.random.nextInt(8) == 0) {
            var state = data == null ? Blocks.SAND.defaultBlockState() : data.getState();
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, state),
                    pos.x + (level.random.nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.random.nextFloat() - .5f) * .5f,
                    0, 0, 0);
        }
        if (data != null) {
            data.playSound(level, pos);
        }
    }

    @Override
    public void morphAirFlow(FanProcessingType.AirFlowParticleAccess access, RandomSource random, @Nullable ParticleData data) {
        int color = data == null ? SANDING_COLOR : data.getColor();
        var state = data == null ? Blocks.SAND.defaultBlockState() : data.getState();
        access.setColor(color);
        access.setAlpha(1f);
        if (random.nextInt(32) == 0)
            access.spawnExtraParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), 0);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide) return;
        entity.clearFire();
    }

    /**
     * Particle data for sanding effect -- stores block state and dust color.
     */
    public static class ParticleData {
        private final BlockState state;
        private final int color;
        private final Set<BlockPos> playedSoundPos = new HashSet<>();

        public ParticleData(BlockState state, int color) {
            this.state = state;
            this.color = color;
        }

        public BlockState getState() { return state; }
        public int getColor() { return color; }

        private static final int MAX_SOUND_POSITIONS = 64;

        public void playSound(Level level, Vec3 pos) {
            if (level.getGameTime() % 7 == 0) {
                if (playedSoundPos.size() < MAX_SOUND_POSITIONS && playedSoundPos.add(BlockPos.containing(pos))) {
                    AllSoundEvents.SANDING_SHORT.playAt(level, pos,
                            0.3F + 0.1F * level.random.nextFloat(),
                            0.9F + 0.2F * level.random.nextFloat(),
                            true);
                }
            } else if (!playedSoundPos.isEmpty()) {
                playedSoundPos.clear();
            }
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
