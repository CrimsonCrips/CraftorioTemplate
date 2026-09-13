# Data Maps

Contracts, effects, and upgrades are all *datapack registries* — separate JSON-backed lists of whole objects. Item/effect/enchantment **point values** work differently: they're a real [NeoForge Data Map](https://docs.neoforged.net/docs/datamaps/), which attaches a single value directly onto an *existing* vanilla or modded registry entry (an `Item`, a `MobEffect`, an `Enchantment`) rather than registering a new object of its own.

## Declaration

Craftorio declares its data maps in `CraftorioDataMaps.java`:

```java
public static final DataMapType<Item, String> POINT_VALUE = DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "point_value"),
                Registries.ITEM,
                ExtraCodecs.NON_EMPTY_STRING)
        .synced(ExtraCodecs.NON_EMPTY_STRING, false)
        .build();

public static void registerDataMaps(RegisterDataMapTypesEvent event) {
    event.register(POINT_VALUE);
    event.register(EFFECT_POINT_VALUE);
    event.register(ENCHANTMENT_POINT_VALUE);
}
```

The value type is `String`, not a number — this lets point values use the mod's scientific-notation parsing (`"1e4"` etc.) for very large numbers, the same way the server config's `STARTING_POINTS` does.

> **Heads up:** Craftorio also *declares* `ADVANCEMENT_POINT_VALUE` and `ADVANCEMENT_MULTIPLIER_VALUE` as `DataMapType` objects, but never passes them to `event.register(...)`. Advancement points are actually read through a separate, hand-rolled `SimpleJsonResourceReloadListener` (`CraftorioAdvancementPoints`) that parses the same JSON shape manually and does a flat overwrite per key — not the real data map merge behavior described below. If you're overriding an advancement's point value, the mechanism differs from items/effects/enchantments even though the JSON looks identical. Everything below applies to `POINT_VALUE`, `EFFECT_POINT_VALUE`, and `ENCHANTMENT_POINT_VALUE`, which are genuinely registered.

## Reading a value

Any code (yours or Craftorio's) reads a data map value straight off the registry holder:

```java
String value = itemStack.getItem().builtInRegistryHolder().getData(CraftorioDataMaps.POINT_VALUE);
```

`getData(...)` returns `null` if nothing set a value for that item — Craftorio's own `checkValue` falls back to `BigInteger.ZERO` in that case.

## Writing values in datagen

You don't hand-write the JSON — you use `DataMapProvider`, the same as Craftorio's own `CraftorioPointsDeterminer`:

```java
public class MyPointsProvider extends DataMapProvider {
    public MyPointsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather() {
        Builder<String, Item> pointValue = this.builder(CraftorioDataMaps.POINT_VALUE);
        // entries go here
    }
}
```

Register it as a normal datagen provider in your `GatherDataEvent` handler — no `RegistrySetBuilder` needed here, data maps aren't registry entries themselves:

```java
event.getGenerator().addProvider(event.includeServer(), new MyPointsProvider(output, provider));
```

### `Builder.add(...)` and the JSON it produces

```java
Builder<T, R> add(Holder<R> object, T value, boolean replace, ICondition... conditions)
Builder<T, R> add(ResourceLocation id, T value, boolean replace, ICondition... conditions)
Builder<T, R> add(TagKey<R> tag, T value, boolean replace, ICondition... conditions)
```

Passing `replace = false` (Craftorio's own datagen always does this) produces a plain value in the JSON:

```json
{
  "values": {
    "minecraft:diamond": "120"
  }
}
```

Passing `replace = true` produces the object form instead:

```json
{
  "values": {
    "minecraft:diamond": { "value": "999", "replace": true }
  }
}
```

For a plain (non-`Advanced`) `DataMapType` like `POINT_VALUE` — which is what Craftorio uses — that per-entry `replace` flag doesn't actually change anything: when two data packs both set a value for the same item, NeoForge's default merge behavior always takes whichever one it resolves *last*, regardless of the flag. The `replace` flag only matters for an `AdvancedDataMapType` configured with a custom merger (for example one that would otherwise *combine* two list values instead of overwriting) — Craftorio doesn't use `AdvancedDataMapType` for point values, so you can leave `replace` as `false` and it behaves identically either way.

## Addition — giving a value to an item that has none

"Addition" just means: the item has no existing `craftorio:point_value` entry from any loaded pack, so there's nothing to conflict with. This is the normal case for your addon's own new items, or for a *third* mod's items that Craftorio itself never assigned a value to:

```java
@Override
protected void gather() {
    Builder<String, Item> pointValue = this.builder(CraftorioDataMaps.POINT_VALUE);

    pointValue.add(MyItems.CUSTOM_INGOT.get(), "500", false);

    pointValue.add(ResourceLocation.fromNamespaceAndPath("somethirdmod", "rare_gem"), "2500", false);
}
```

The second line shows registering a value for an item your own mod doesn't even own — this is exactly the pattern Craftorio's own (currently unused) `CraftorioExternalPointsDeterminer` sets up for assigning point values to Create's items from inside Craftorio itself. Any mod can add a `craftorio:point_value` entry for any item in any namespace; there's nothing special about owning the item.

## Replacement — overriding a value Craftorio already assigned

"Replacement" means targeting an item that **already** has a `craftorio:point_value` entry — most commonly a vanilla item, since Craftorio assigns point values to most of the vanilla item set out of the box (`minecraft:diamond` is `"120"` by default). Writing your own entry for the same item in your own mod's data:

```java
@Override
protected void gather() {
    Builder<String, Item> pointValue = this.builder(CraftorioDataMaps.POINT_VALUE);

    pointValue.add(Items.DIAMOND, "9999", false);
}
```

This generates `data/<your_modid>/data_maps/item/point_value.json` (note: **your own mod's namespace**, not `craftorio`) alongside Craftorio's own `data/craftorio/data_maps/item/point_value.json`. Both files contribute to the same `craftorio:point_value` data map, and since your addon depends on (and loads after) Craftorio, your entry for `minecraft:diamond` is the one that wins — no special JSON field is required for the common "my addon overrides Craftorio's own defaults" case. If you ever do need to force a value to win in a situation where the load order isn't in your favor, that's what `replace: true` is for on an `AdvancedDataMapType` — but again, `POINT_VALUE` itself doesn't need it.

`EFFECT_POINT_VALUE` (keyed on `MobEffect`) and `ENCHANTMENT_POINT_VALUE` (keyed on `Enchantment`) work identically — same `Builder<String, R>` API, same addition/replacement behavior, just a different registry type.
