# CraftorioTemplate Wiki

This is an example addon project for [Craftorio](https://github.com/CrimsonCrips/Craftorio), showing how to add your own content to it from a separate mod — no fork required.

Every piece of addable content in Craftorio (contracts, effects, upgrades) is stored in a **NeoForge datapack registry**. That means your addon doesn't need to touch Craftorio's source at all: you write your own datagen classes that register entries under Craftorio's registry keys, and NeoForge merges your JSON output with Craftorio's own at load time, exactly the way vanilla merges recipes or loot tables contributed by different mods.

Before reading any of the pages below, make sure your project is wired up as described in the main [README](../README.md) — `mavenLocal()` + the third-party repos Craftorio itself depends on (Create, Registrate, Xaero's map, CurseMaven), plus `implementation "org.craftorio:Craftorio:<version>"`.

## Pages

- [Making a Contract](Making-a-Contract.md) — bounty items, data-component matching, rewards, punishments, the contract bootstrap.
- [Making an Effect](Making-an-Effect.md) — the three effect types, the `unobtainable` flag, the effect bootstraps.
- [Making an Upgrade](Making-an-Upgrade.md) — datagen (`CraftorioModifierUpgrade`, attribute upgrades) vs. manual (`ActionUpgrade`) upgrade types, and how a new upgrade shows up in the skill tree automatically.
- [Data Maps](Data-Maps.md) — how Craftorio assigns point values to items/effects/enchantments via real NeoForge Data Maps, with worked addition and replacement examples.

## The shared pattern

All three registries (`CraftorioContract`, `CraftorioEffects`, `CraftorioUpgrade`) are wired into Craftorio's own datagen the same way, in `CraftorioDatagen.java`:

```java
RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
        .add(CraftorioEffects.REGISTRY_KEY, context -> { ... })
        .add(CraftorioContract.REGISTRY_KEY, CraftorioContractBootstrap::bootstrap)
        .add(CraftorioUpgrade.REGISTRY_KEY, CraftorioUpgradeBootstrap::bootstrap);

generator.addProvider(event.includeServer(),
        new DatapackBuiltinEntriesProvider(output, provider, registryBuilder, Set.of(Craftorio.MODID)));
```

A **bootstrap** is just a method matching `void bootstrap(BootstrapContext<T> context)` that calls `context.register(key, instance)` for every entry it wants to add. `DatapackBuiltinEntriesProvider` runs every registered bootstrap during `runData` and serializes the results to JSON under `data/<contributing-namespace>/craftorio/<registry-path>/<id>.json` — for example the `hephaestus_prison` contract, registered under the `craftorio` namespace, generates to `data/craftorio/craftorio/contract/hephaestus_prison.json`.

The **last `Set.of(Craftorio.MODID)` argument matters**: it tells `DatapackBuiltinEntriesProvider` which namespace(s) *this provider instance* is responsible for writing. Your addon needs its own `GatherDataEvent` handler with its own `RegistrySetBuilder` and its own `DatapackBuiltinEntriesProvider` passing `Set.of(YourModId.MODID)` — each mod generates only its own entries, and NeoForge loads all of them together at runtime from every namespace's `data/<namespace>/craftorio/<registry-path>/` folder. Each content page below shows this concretely for its registry.
