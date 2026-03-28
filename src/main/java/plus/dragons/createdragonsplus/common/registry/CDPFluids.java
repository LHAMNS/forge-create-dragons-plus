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

package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.api.event.PipeCollisionEvent;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.FluidEntry;
import java.util.EnumMap;
import net.minecraft.tags.ItemTags;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createdragonsplus.client.color.SimpleItemColors;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.StandardDispenserBehaviour;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonBreathFluidType;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonBreathLiquidBlock;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonsBreathOpenPipeEffect;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeFluidOpenPipeEffect;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeFluidType;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeLiquidBlock;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.config.ConfigFeatureCondition;
import plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders;
import plus.dragons.createdragonsplus.util.RegistryAliases;

public class CDPFluids {
    public static final ModTags MOD_TAGS = new ModTags();
    public static final CommonTags COMMON_TAGS = new CommonTags();
    public static final EnumMap<DyeColor, FluidEntry<ForgeFlowingFluid.Flowing>> DYES_BY_COLOR = Util.make(
            new EnumMap<>(DyeColor.class),
            map -> {
                for (var color : DyeColors.ALL) map.put(color, dye(color));
            });
    public static final FluidEntry<ForgeFlowingFluid.Flowing> DRAGON_BREATH = REGISTRATE
            .fluid("dragon_breath",
                    new ResourceLocation(CDPCommon.ID, "fluid/dragon_breath_still"),
                    new ResourceLocation(CDPCommon.ID, "fluid/dragon_breath_flow"),
                    DragonBreathFluidType.create())
            .lang("Dragon's Breath")
            .properties(properties -> properties
                    .rarity(Rarity.UNCOMMON)
                    .density(3000)
                    .viscosity(6000)
                    .lightLevel(15)
                    .motionScale(0.07)
                    .canSwim(false)
                    .canDrown(false)
                    .pathType(BlockPathTypes.DAMAGE_OTHER)
                    .adjacentPathType(null)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.DRAGON_FIREBALL_EXPLODE)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA))
            .fluidProperties(properties -> properties
                    .explosionResistance(100F)
                    .levelDecreasePerBlock(2)
                    .slopeFindDistance(2)
                    .tickRate(30))
            .source(ForgeFlowingFluid.Source::new)
            .tag(COMMON_TAGS.dragonBreath, MOD_TAGS.fanEndingCatalysts)
            .block(DragonBreathLiquidBlock::new)
            .lang("Dragon's Breath")
            .build()
            .bucket()
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .lang("Dragon's Breath Bucket")
            .tag(CDPItems.DRAGON_BREATH_BUCKETS)
            .build()
            .setData(ProviderType.RECIPE, (ctx, prov) -> {
                CreateRecipeBuilders.emptying(ctx.getId().withPath("dragon_breath"))
                        .require(Items.DRAGON_BREATH)
                        .output(ctx.get(), 250)
                        .output(Items.GLASS_BOTTLE)
                        .withCondition(new ConfigFeatureCondition("dragonBreathFluid"))
                        .build(prov);
                CreateRecipeBuilders.filling(ctx.getId().withPath("dragon_breath"))
                        .require(ctx.get(), 250)
                        .require(Items.GLASS_BOTTLE)
                        .output(Items.DRAGON_BREATH)
                        .withCondition(new ConfigFeatureCondition("dragonBreathFluid"))
                        .build(prov);
            })
            .register();

    public static void register(IEventBus modBus) {
        modBus.register(CDPFluids.class);
        // Upstream NeoForge registers these via BuiltInRegistries.FLUID.addAlias().
        // On Forge 1.20.1, we emulate this via RegistryAliases + RegistryAliasMixin on MappedRegistry.
        // This ensures world data referencing old fluid IDs (e.g. in pipes/tanks) loads correctly.
        RegistryAliases.addAlias(
                new ResourceLocation(CDPCommon.ID, "dragons_breath"),
                new ResourceLocation(CDPCommon.ID, "dragon_breath"));
        RegistryAliases.addAlias(
                new ResourceLocation(CDPCommon.ID, "flowing_dragons_breath"),
                new ResourceLocation(CDPCommon.ID, "flowing_dragon_breath"));
    }

    public static void registerDispenserBehavior() {
        DYES_BY_COLOR.values().forEach(dyeFluid -> DispenserBlock.registerBehavior(dyeFluid.getBucket().get(), StandardDispenserBehaviour.INSTANCE));
        DispenserBlock.registerBehavior(DRAGON_BREATH.getBucket().get(), StandardDispenserBehaviour.INSTANCE);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Reactions::registerFluidInteractions);
        event.enqueueWork(Reactions::registerOpenPipeEffects);
        event.enqueueWork(CDPFluids::registerDispenserBehavior);
    }

    private static FluidEntry<ForgeFlowingFluid.Flowing> dye(DyeColor color) {
        var stillTexture = new ResourceLocation(CDPCommon.ID, "fluid/dye_still");
        var flowingTexture = new ResourceLocation(CDPCommon.ID, "fluid/dye_flow");
        var tintColor = 0xFF000000 | color.getFireworkColor();
        var name = color.getName() + "_dye";
        var tag = COMMON_TAGS.dyesByColor.get(color);
        return REGISTRATE.fluid(name, stillTexture, flowingTexture, DyeFluidType.create(color))
                .properties(properties -> properties
                        .fallDistanceModifier(0f)
                        .canExtinguish(true)
                        .supportsBoating(true)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                        .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH))
                .fluidProperties(properties -> properties.explosionResistance(100))
                .block((fluid, prop) -> new DyeLiquidBlock(color, fluid, prop))
                .build()
                .source(ForgeFlowingFluid.Source::new)
                .bucket()
                .tag(CDPItems.DYE_BUCKETS_BY_COLOR.getOrDefault(color, CDPItems.DYE_BUCKETS))
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("dye_bucket")))
                .color(() -> SimpleItemColors.singleLayer(tintColor))
                .tag(ItemTags.create(new ResourceLocation("forge", "dyed/" + color.getName())))
                .build()
                .tag(tag)
                .setData(ProviderType.RECIPE, (ctx, prov) -> {
                    CreateRecipeBuilders.mixing(ctx.getId().withPath(name + "_from_item"))
                            .require(DyeItem.byColor(color))
                            .require(Fluids.WATER, 250)
                            .output(ctx.get(), 250)
                            .withCondition(new ConfigFeatureCondition("dyeFluids"))
                            .build(prov);
                    CreateRecipeBuilders.mixing(ctx.getId().withPath(name + "_from_fluid"))
                            .require(ctx.get(), 250)
                            .output(DyeItem.byColor(color))
                            .requiresHeat(HeatCondition.HEATED)
                            .withCondition(new ConfigFeatureCondition("dyeFluids"))
                            .build(prov);
                })
                .register();
    }

    public static class ModTags {
        public final TagKey<Fluid> fanEndingCatalysts = TagKey.create(Registries.FLUID,
                new ResourceLocation(CDPCommon.ID, "fan_processing_catalysts/ending"));

        public ModTags() {
        }
    }

    public static class CommonTags {
        public final TagKey<Fluid> dyes = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "dyes"));
        public final EnumMap<DyeColor, TagKey<Fluid>> dyesByColor = Util.make(new EnumMap<>(DyeColor.class), map -> {
            for (var color : DyeColors.ALL) {
                map.put(color, TagKey.create(Registries.FLUID, new ResourceLocation("forge", "dyes/" + color.getName())));
            }
        });
        public final TagKey<Fluid> dragonBreath = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "dragon_breath"));

        protected CommonTags() {
        }
    }

    @Mod.EventBusSubscriber
    public static class Reactions {
        private static final Map<FluidType, BlockState> LAVA_INTERACTIONS = new HashMap<>();

        @SubscribeEvent
        public static void onPipeCollisionFlow(final PipeCollisionEvent.Flow event) {
            FluidType first = event.getFirstFluid().getFluidType();
            FluidType second = event.getSecondFluid().getFluidType();
            if (first == ForgeMod.LAVA_TYPE.get() && LAVA_INTERACTIONS.containsKey(second)) {
                event.setState(LAVA_INTERACTIONS.get(second));
            } else if (second == ForgeMod.LAVA_TYPE.get() && LAVA_INTERACTIONS.containsKey(first)) {
                event.setState(LAVA_INTERACTIONS.get(first));
            }
        }

        @SubscribeEvent
        public static void onPipeCollisionSpill(final PipeCollisionEvent.Spill event) {
            Fluid world = event.getWorldFluid();
            Fluid pipe = event.getPipeFluid();
            FluidType worldType = world.getFluidType();
            FluidType pipeType = pipe.getFluidType();
            if (worldType == ForgeMod.LAVA_TYPE.get() && LAVA_INTERACTIONS.containsKey(pipeType)) {
                if (world.isSource(world.defaultFluidState())) {
                    event.setState(Blocks.OBSIDIAN.defaultBlockState());
                } else {
                    event.setState(LAVA_INTERACTIONS.get(pipeType));
                }
            } else if (pipeType == ForgeMod.LAVA_TYPE.get() && LAVA_INTERACTIONS.containsKey(worldType)) {
                if (pipe.isSource(pipe.defaultFluidState())) {
                    event.setState(Blocks.OBSIDIAN.defaultBlockState());
                } else {
                    event.setState(LAVA_INTERACTIONS.get(worldType));
                }
            }
        }

        static void registerFluidInteractions() {
            var genConcrete = CDPConfig.common().dyeFluidsLavaInteractionGenerateColoredConcrete.get();
            DYES_BY_COLOR.forEach((color, entry) -> {
                var type = entry.getType();
                // Upstream NeoForge uses ResourceLocation.parse(color.getName()).withSuffix("_concrete").
                // This is equivalent: DyeColor.getName() returns plain lowercase names (e.g. "red"),
                // so both resolve to "minecraft:<color>_concrete". DyeColor is a vanilla enum with
                // no modded entries, so the namespace is always "minecraft".
                var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft", color.getName() + "_concrete"));
                if (block == null || block == Blocks.AIR)
                    return;
                LAVA_INTERACTIONS.put(type, genConcrete ? block.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
                FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new InteractionInformation(
                        type,
                        fluidState -> fluidState.isSource()
                                ? Blocks.OBSIDIAN.defaultBlockState()
                                : genConcrete ? block.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState()));
            });
            LAVA_INTERACTIONS.put(DRAGON_BREATH.getType(), Blocks.END_STONE.defaultBlockState());
            FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new InteractionInformation(
                    DRAGON_BREATH.getType(),
                    fluidState -> fluidState.isSource()
                            ? Blocks.OBSIDIAN.defaultBlockState()
                            : Blocks.END_STONE.defaultBlockState()));
        }

        static void registerOpenPipeEffects() {
            DYES_BY_COLOR.forEach((color, entry) -> OpenPipeEffectHandler.REGISTRY.register(entry.getSource(), new DyeFluidOpenPipeEffect(color)));
            OpenPipeEffectHandler.REGISTRY.register(DRAGON_BREATH.getSource(), new DragonsBreathOpenPipeEffect());
        }
    }
}
