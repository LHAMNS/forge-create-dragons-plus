# Changelog

All notable changes to this project will be documented in this file.

## [1.0.1-mc1.20.1-fix1] - 2026-04-05

### Fixed
- **[FATAL] Config access during registry freeze**: `FanProcessingTypeMixinForGarnished` called `CDPConfig.server().enableBulkFreezing.get()` during `FanProcessingTypeRegistry.init()`, before config was loaded. Current Forge logs a warning; future versions will crash in production too. Added `isLoaded()` guard with safe default.
- **Mixin SRG/MCP dual-environment compatibility** (6 mixins):
  - `ContraptionMixin`: `@At` target `m_7731_` changed to `setBlock` with `remap = true`
  - `FluidFillingBehaviourMixin`: `@At` target `m_6263_` changed to `playSound` with `remap = true`
  - `OpenEndedPipeMixin`: `@At` target `f_63857_` changed to `ultraWarm` with `remap = true`
  - `AirFlowParticleMixin`: `method` changed to dual-name `{"m_5989_", "tick"}` with `require = 1`
  - `BottleItemMixin`: `method` changed to dual-name `{"m_289173_", "lambda$use$0"}` with `require = 1`
  - These mixins previously only worked in production (SRG names); they now work in both dev (MCP) and production (SRG) environments

### Added
- `CDPRegistryIntegrationTest`: 8 GameTests verifying all 16 dye fluids, Dragon's Breath fluid, Fluid Hatch block, 4 fan processing types, 4 recipe types, DataMap coloring catalysts, config accessibility, and Blaze Upgrade Template registration
- CEI + CIF as `runtimeOnly fg.deobf()` dependencies in build.gradle for three-mod client integration testing

## [1.0.0-mc1.20.1] - 2026-03-28

### Added
- Complete port of Create: Dragons Plus from NeoForge 1.21.1 to Forge 1.20.1
- Bulk Coloring via dye fluids + fan processing (16 colors)
- Bulk Freezing via powder snow + fan processing
- Bulk Ending via Dragon's Breath + fan processing
- Bulk Sanding via falling sand + fan processing
- Fluid Hatch block for fluid storage in contraptions
- Dragon's Breath as a pumpable fluid with automatic brewing recipes
- Blaze Upgrade Smithing Template (Nether loot)
- Full config system with per-feature toggles
- JEI integration for all fan processing categories
- Ponder scenes for all bulk processing types
- Create: Dreams & Desires compatibility (conditional fan type disabling)
- Create: Garnished compatibility (mastic resin coloring catalysts)
- Runtime recipe generation for oxidation/waxing sandpaper polishing
- Fluid registry aliases for backward compatibility
- Complete Chinese (zh_cn) localization

### Fixed (upstream bugs)
- Dragon's Breath brewing recipes no longer duplicate on `/reload`
- Fluid pipe consuming effects no longer fire during simulation queries
- DnD/Garnished mixins changed from interface to abstract class (prevents InvalidInterfaceMixinException)

### Platform Adaptations
- NeoForge DeferredHolder -> custom DeferredHolder with thread-safe lazy resolution
- NeoForge RecipeHolder -> custom RecipeHolder record
- NeoForge DataMapType -> HashMap-based registry with tag support
- NeoForge StatAwardEvent -> ServerPlayerMixin injection
- NeoForge BlockDropsEvent -> custom HarvestDropsModifyEvent
- NeoForge Registry.addAlias() -> RegistryAliasMixin
- NeoForge ConfigFeatureCondition -> Forge ICondition implementation
- All Mixin targets verified with SRG names for runtime compatibility
- conditional-mixin embedded via JarJar (no separate installation needed)
