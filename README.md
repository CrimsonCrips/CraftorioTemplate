# CraftorioTemplate

An example addon for [Craftorio](https://github.com/CrimsonCrips/Craftorio) — shows how to register your own contracts, effects, upgrades, and item point values from a separate mod, no fork required.

## Getting started

1. **Clone and publish Craftorio locally.** This template depends on Craftorio via Maven, and it isn't on a public repository yet, so you need a local copy:

   ```bash
   git clone https://github.com/CrimsonCrips/Craftorio
   cd Craftorio
   ./gradlew publishToMavenLocal
   ```

2. **Clone this repo** and open it in IntelliJ (or your IDE of choice).

3. **Run it.** `./gradlew runClient` boots a dev client with both Craftorio and this template loaded, so you can see the example content in-game.

That's it — everything in `src/main/java/org/crimsoncrips/craftoriotemplate/` is real, working example code, already wired into `runData`.

## Learn how it works

The [wiki](https://github.com/CrimsonCrips/CraftorioTemplate/wiki) walks through each system with real code from this repo:

- [Making a Contract](https://github.com/CrimsonCrips/CraftorioTemplate/wiki/Making-a-Contract)
- [Making an Effect](https://github.com/CrimsonCrips/CraftorioTemplate/wiki/Making-an-Effect)
- [Making an Upgrade](https://github.com/CrimsonCrips/CraftorioTemplate/wiki/Making-an-Upgrade) / [Modifier Targets](https://github.com/CrimsonCrips/CraftorioTemplate/wiki/Modifier-Targets)
- [Data Maps](https://github.com/CrimsonCrips/CraftorioTemplate/wiki/Data-Maps)
