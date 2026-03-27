/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import static plus.dragons.createdragonsplus.common.CDPCommon.PERSISTENT_DATA_KEY;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.ModIntegration;
import plus.dragons.createdragonsplus.util.DeferredHolder;
import plus.dragons.createdragonsplus.util.PersistentDataHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Coloring fan processing type. Implements {@link FanProcessingType} directly,
 * matching the upstream NeoForge structure. Uses {@link DeferredHolder} for
 * Create Garnished compat lookups.
 */
public class ColoringFanProcessingType implements FanProcessingType {
    // Static cache for coloring recipes grouped by DyeColor
    private static volatile Map<DyeColor, List<Recipe<?>>> coloringRecipesByColor = Collections.emptyMap();
    private static volatile RecipeManager lastRecipeManager;

    /**
     * Get coloring recipes pre-filtered for a specific DyeColor.
     * Rebuilds the cache when the RecipeManager instance changes (recipe reload).
     */
    @SuppressWarnings("unchecked")
    public static List<Recipe<?>> getRecipesForColor(DyeColor color, Level level) {
        RecipeManager currentManager = level.getRecipeManager();
        if (currentManager != lastRecipeManager) {
            Map<DyeColor, List<Recipe<?>>> newMap = new EnumMap<>(DyeColor.class);
            for (DyeColor c : DyeColor.values()) {
                newMap.put(c, new ArrayList<>());
            }
            for (Recipe<?> recipe : currentManager.getAllRecipesFor(CDPRecipes.COLORING.getType())) {
                if (recipe instanceof ColoringRecipe coloringRecipe) {
                    newMap.get(coloringRecipe.getColor()).add(recipe);
                }
            }
            for (DyeColor c : DyeColor.values()) {
                newMap.put(c, Collections.unmodifiableList(newMap.get(c)));
            }
            coloringRecipesByColor = Collections.unmodifiableMap(newMap);
            lastRecipeManager = currentManager;
        }
        return coloringRecipesByColor.getOrDefault(color, Collections.emptyList());
    }

    private final DyeColor color;
    private final Vector3f rgb;
    private final int packedColor;
    private final DustParticleOptions cachedParticle;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> createGarnishedRecipe;
    private final RecipeWrapper reusableWrapper =
            new RecipeWrapper(new net.minecraftforge.items.ItemStackHandler(1));
    private final CraftingContainer cachedContainer1x2 = createCraftingContainer(2, 1);
    private final CraftingContainer cachedContainer3x3 = createCraftingContainer(3, 3);

    public ColoringFanProcessingType(DyeColor color) {
        this.color = color;
        float[] components = color.getTextureDiffuseColors();
        this.rgb = new Vector3f(components[0], components[1], components[2]);
        int r = (int) (components[0] * 255) & 0xFF;
        int g = (int) (components[1] * 255) & 0xFF;
        int b = (int) (components[2] * 255) & 0xFF;
        this.packedColor = (r << 16) | (g << 8) | b;
        this.cachedParticle = new DustParticleOptions(new Vector3f(this.rgb), 2);
        this.createGarnishedRecipe = DeferredHolder.create(
                net.minecraft.core.registries.Registries.RECIPE_TYPE,
                ModIntegration.CREATE_GARNISHED.asResource(color.getSerializedName() + "_dye_blowing"));
    }

    public DyeColor getColor() {
        return color;
    }

    public DustParticleOptions getParticle() {
        return cachedParticle;
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!CDPConfig.server().enableBulkColoring.get()) return false;
        if (CDPDataMaps.getFluidColoringCatalyst(level.getFluidState(pos)) == this.color)
            return true;
        return CDPDataMaps.getBlockColoringCatalyst(level.getBlockState(pos)) == this.color;
    }

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        if (!CDPConfig.server().enableBulkColoring.get()) return false;
        Optional<ColoringRecipe> recipe = findColoringRecipe(stack, level);
        if (recipe.isPresent()) return true;
        if (canProcessByCreateGarnished(stack, level)) return true;
        return this.processByCrafting(stack, level).isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        Optional<ColoringRecipe> recipe = findColoringRecipe(stack, level);
        if (recipe.isPresent()) {
            return RecipeApplier.applyRecipeOn(level, stack, recipe.get(), true);
        }
        Optional<List<ItemStack>> garnishedResult = processByCreateGarnished(stack, level);
        if (garnishedResult.isPresent()) {
            return garnishedResult.get();
        }
        Optional<ItemStack> craftingResult = processByCrafting(stack, level);
        if (craftingResult.isPresent()) {
            return ItemHelper.multipliedOutput(stack, craftingResult.get());
        }
        return null;
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.random.nextInt(8) == 0) {
            level.addParticle(cachedParticle,
                    pos.x + (level.random.nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.random.nextFloat() - .5f) * .5f,
                    0, 1 / 8f, 0);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess access, RandomSource random) {
        access.setColor(packedColor);
        access.setAlpha(1f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide) return;
        if (entity instanceof LivingEntity livingEntity) {
            applyColoring(livingEntity, level);
        }
        if (entity instanceof EnderMan
                || entity.getType() == EntityType.SNOW_GOLEM
                || entity.getType() == EntityType.BLAZE) {
            entity.hurt(entity.damageSources().drown(), 2);
        }
        if (entity.isOnFire()) {
            entity.clearFire();
            level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.NEUTRAL, 0.7F,
                    1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
        }
    }

    /**
     * Apply coloring to a living entity: sets colors on sheep, cat collar, wolf collar,
     * and dyes equipped armor/items.
     */
    public void applyColoring(LivingEntity entity, Level level) {
        if (processColoring(entity)) {
            if (entity instanceof Sheep sheep) {
                sheep.setColor(this.color);
            } else if (entity instanceof Shulker shulker) {
                shulker.getEntityData().set(plus.dragons.createdragonsplus.mixin.minecraft.ShulkerAccessor.getDataColorId(), (byte) this.color.getId());
            } else if (entity instanceof Cat cat) {
                cat.setCollarColor(this.color);
            } else if (entity instanceof Wolf wolf) {
                wolf.setCollarColor(this.color);
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack equipped = entity.getItemBySlot(slot);
                if (equipped.isEmpty()) continue;
                applyColoringToItem(equipped, level).ifPresent(result -> {
                    result.setCount(equipped.getCount());
                    entity.setItemSlot(slot, result);
                });
            }
        }
    }

    private boolean processColoring(LivingEntity entity) {
        CompoundTag nbt = PersistentDataHelper.getOrCreate(entity.getPersistentData(), PERSISTENT_DATA_KEY, "Coloring");
        int sinceLastProcess = 0;
        if (!(nbt.contains("Color", Tag.TAG_STRING) && nbt.getString("Color").equals(this.color.getName()))) {
            nbt.putString("Color", this.color.getName());
            nbt.remove("Time");
        } else if (nbt.contains("LastProcess", Tag.TAG_INT)) {
            int lastProcess = nbt.getInt("LastProcess");
            sinceLastProcess = entity.tickCount - lastProcess - 1;
        }
        nbt.putInt("LastProcess", entity.tickCount);
        int processingTime = AllConfigs.server().kinetics.fanProcessingTime.get();
        if (!nbt.contains("Time", Tag.TAG_INT) || sinceLastProcess < 0) {
            nbt.putInt("Time", processingTime);
            return false;
        }
        int time = nbt.getInt("Time") + sinceLastProcess;
        if (time == 0) {
            nbt.remove("Color");
            nbt.remove("LastProcess");
            nbt.remove("Time");
            return true;
        }
        nbt.putInt("Time", Math.min(processingTime, time - 1));
        return false;
    }

    private Optional<ItemStack> applyColoringToItem(ItemStack stack, Level level) {
        Optional<ColoringRecipe> coloringRecipe = findColoringRecipe(stack, level);
        if (coloringRecipe.isPresent()) {
            reusableWrapper.setItem(0, stack);
            ItemStack result = coloringRecipe.get().assemble(reusableWrapper, level.registryAccess());
            return Optional.of(result);
        }
        return processByCrafting(stack, level);
    }

    @SuppressWarnings("unchecked")
    private boolean canProcessByCreateGarnished(ItemStack stack, Level level) {
        if (!ModIntegration.CREATE_GARNISHED.enabled()) return false;
        if (!createGarnishedRecipe.isBound()) return false;
        reusableWrapper.setItem(0, stack);
        return level.getRecipeManager().getRecipeFor((RecipeType) createGarnishedRecipe.get(), reusableWrapper, level).isPresent();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Optional<List<ItemStack>> processByCreateGarnished(ItemStack stack, Level level) {
        if (!ModIntegration.CREATE_GARNISHED.enabled()) return Optional.empty();
        if (!createGarnishedRecipe.isBound()) return Optional.empty();
        reusableWrapper.setItem(0, stack);
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> opt =
                level.getRecipeManager().getRecipeFor((RecipeType) createGarnishedRecipe.get(), reusableWrapper, level);
        if (opt.isEmpty()) return Optional.empty();
        return Optional.of(RecipeApplier.applyRecipeOn(level, stack, opt.get(), true));
    }

    private Optional<ItemStack> processByCrafting(ItemStack stack, Level level) {
        if (stack.is(CDPItems.NOT_APPLICABLE_COLORING))
            return Optional.empty();
        // 1 Dye + 1 Colorless = 1 Dyed
        cachedContainer1x2.setItem(0, stack.copy());
        cachedContainer1x2.setItem(1, new ItemStack(DyeItem.byColor(this.color)));
        var optional = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, cachedContainer1x2, level);
        if (optional.isPresent()) {
            var craftRecipe = optional.get();
            var result = craftRecipe.assemble(cachedContainer1x2, level.registryAccess());
            cachedContainer1x2.clearContent();
            if (result.getCount() == 1) return Optional.of(result);
        } else {
            cachedContainer1x2.clearContent();
        }
        // 1 Dye + 8 Colorless = 8 Dyed
        for (int i = 0; i < 9; i++) {
            cachedContainer3x3.setItem(i, stack.copy());
        }
        cachedContainer3x3.setItem(4, new ItemStack(DyeItem.byColor(this.color)));
        optional = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, cachedContainer3x3, level);
        if (optional.isPresent()) {
            var craftRecipe = optional.get();
            var result = craftRecipe.assemble(cachedContainer3x3, level.registryAccess());
            cachedContainer3x3.clearContent();
            if (result.getCount() != 8) return Optional.empty();
            result.setCount(1);
            return Optional.of(result);
        }
        cachedContainer3x3.clearContent();
        return Optional.empty();
    }

    private Optional<ColoringRecipe> findColoringRecipe(ItemStack stack, Level level) {
        for (var r : getRecipesForColor(this.color, level)) {
            if (r instanceof ColoringRecipe coloringRecipe
                    && !coloringRecipe.getIngredients().isEmpty()
                    && coloringRecipe.getIngredients().get(0).test(stack)) {
                return Optional.of(coloringRecipe);
            }
        }
        return Optional.empty();
    }

    private static CraftingContainer createCraftingContainer(int width, int height) {
        return new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
                return ItemStack.EMPTY;
            }
            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                return false;
            }
        }, width, height);
    }
}
