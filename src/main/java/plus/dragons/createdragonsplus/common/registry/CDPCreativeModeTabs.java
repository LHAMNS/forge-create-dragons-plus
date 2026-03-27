/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.registry.CDPBlocks.*;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.*;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.config.CDPConfig;

public class CDPCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CDPCommon.ID);

    public static final RegistryObject<CreativeModeTab> BASE = TABS.register("base", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CDPCommon.ID + ".base"))
                    .withTabsBefore(AllCreativeModeTabs.BASE_CREATIVE_TAB.getId())
                    .icon(RARE_MARBLE_GATE_PACKAGE::asStack)
                    .displayItems(CDPCreativeModeTabs::buildBaseContents)
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private static void buildBaseContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (CDPConfig.common().fluidHatch.get())
            output.accept(FLUID_HATCH);
        if (CDPConfig.common().blazeUpgradeSmithingTemplate.get())
            output.accept(BLAZE_UPGRADE_SMITHING_TEMPLATE);
        if (CDPConfig.common().dyeFluids.get())
            for (var color : DyeColors.CREATIVE_MODE_TAB) {
                CDPFluids.DYES_BY_COLOR.get(color).getBucket().ifPresent(output::accept);
            }
        if (CDPConfig.common().dragonBreathFluid.get())
            CDPFluids.DRAGON_BREATH.getBucket().ifPresent(output::accept);
        output.accept(RARE_BLAZE_PACKAGE, TabVisibility.SEARCH_TAB_ONLY);
        output.accept(RARE_MARBLE_GATE_PACKAGE, TabVisibility.SEARCH_TAB_ONLY);
    }
}
