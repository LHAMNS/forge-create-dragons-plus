/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported from NeoForge 1.21.1 to Forge 1.20.1
 */
package plus.dragons.createdragonsplus.common;

import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createdragonsplus.common.registry.CDPBlockFreezers;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPCapabilities;
import plus.dragons.createdragonsplus.common.registry.CDPConditions;
import plus.dragons.createdragonsplus.common.registry.CDPCreativeModeTabs;
import plus.dragons.createdragonsplus.common.registry.CDPCriterions;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItemAttributes;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.data.internal.CDPRegistrateDataMaps;
import plus.dragons.createdragonsplus.data.internal.CDPRuntimeRecipeProvider;
import plus.dragons.createdragonsplus.data.runtime.RuntimePackResources;
import plus.dragons.createdragonsplus.integration.ModIntegration;

@Mod(CDPCommon.ID)
public class CDPCommon {
    public static final String ID = "create_dragons_plus";
    public static final String NAME = "Create: Dragons Plus";
    public static final String PERSISTENT_DATA_KEY = "CreateDragonsPlusData";
    public static final CDPRegistrate REGISTRATE = (CDPRegistrate) new CDPRegistrate(ID)
            .setTooltipModifier(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));

    private final Component runtimePackTitle = Component.literal(NAME);
    private final Component runtimePackDescription = Component.literal(NAME + " Runtime Generated Resources");

    public CDPCommon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CDPConfig.register();
        CDPConditions.register();
        REGISTRATE.registerEventListeners(modBus);
        CDPFluids.register(modBus);
        CDPBlocks.register(modBus);
        CDPBlockEntities.register(modBus);
        CDPItems.register(modBus);
        CDPCreativeModeTabs.register(modBus);
        REGISTRATE.setCreativeModeTab(CDPCreativeModeTabs.BASE);
        CDPRecipes.register(modBus);
        CDPFanProcessingTypes.register();
        CDPCapabilities.register(modBus);
        CDPItemAttributes.register(modBus);
        CDPCriterions.register();
        CDPDataMaps.register();
        modBus.addListener(this::construct);
        modBus.addListener(this::setup);
        modBus.addListener(this::addPackFinders);
    }

    private void construct(final FMLConstructModEvent event) {
        for (ModIntegration integration : ModIntegration.values()) {
            if (integration.enabled())
                event.enqueueWork(integration::onConstructMod);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(CDPBlockFreezers::register);
        // Register Garnished coloring catalysts after all mods have registered their fluids
        if (ModIntegration.CREATE_GARNISHED.enabled()) {
            event.enqueueWork(CDPRegistrateDataMaps::registerGarnishedColoringCatalysts);
        }
        for (ModIntegration integration : ModIntegration.values()) {
            if (integration.enabled())
                event.enqueueWork(integration::onCommonSetup);
        }
    }

    /**
     * Registers the runtime resource pack for server data (recipes generated at runtime).
     * Matches the original NeoForge AddPackFindersEvent handler.
     */
    private void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            var modContainer = ModList.get().getModContainerById(ID).orElseThrow();
            var pack = new RuntimePackResources("runtime", modContainer, PackType.SERVER_DATA,
                    Pack.Position.TOP, runtimePackTitle, runtimePackDescription);
            pack.addDataProvider(new CDPRuntimeRecipeProvider(pack.getPackOutput()));
            event.addRepositorySource(pack);
        }
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(ID, path);
    }
}
