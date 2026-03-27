# Changelog

All notable changes to this project will be documented in this file.

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
