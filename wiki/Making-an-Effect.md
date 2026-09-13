# Making an Effect

An effect is one entry in the `craftorio:effect` datapack registry (`CraftorioEffects.REGISTRY_KEY`). Effects are timed buffs or debuffs — a multiplier that applies for a set duration, then expires and removes itself.

## The three effect types

All three extend the abstract `CraftorioEffects`, which holds the fields every effect shares: `name` (a lang key), `time` (ticks remaining), `icon`, `weight` (roll weight when randomly granted), and `unobtainable` (see below).

| Type | Extra field | Effect |
|---|---|---|
| `GeneralMultiplierEffect` | — | flat `float multiplier`, folded into the player's overall Craftorio points multiplier |
| `TagMultiplierEffect` | `TagKey<Item> itemTag` | the multiplier only applies to items in that tag (`getTagMultiplier(stack)` returns `0F` for anything outside the tag) |
| `ShopMultiplierEffect` | — | flat `float multiplier` applied to shop pricing instead of point gain |

A general effect:

```java
new GeneralMultiplierEffect(multiplier, "registry.my_effect_name", seconds, icon, weight, unobtainable)
```

A tag-scoped effect (only affects items in a specific tag):

```java
new TagMultiplierEffect(100F, "registry.copper_block_buff", CraftorioItemTagGen.COPPER, 25, icon, 10, true)
```

A shop effect has three constructors — **use the 6-argument one explicitly** if you want full control:

```java
// 6-arg: full control, name key used as-is
new ShopMultiplierEffect(multiplier, "registry.my_shop_effect", seconds, icon, weight, unobtainable)

// 5-arg #1: weight hardcoded to 0, name auto-prefixed with "registry."
new ShopMultiplierEffect(multiplier, "my_shop_effect", seconds, icon, unobtainable)

// 5-arg #2: unobtainable hardcoded to false, name auto-prefixed with "registry."
new ShopMultiplierEffect(multiplier, "my_shop_effect", seconds, icon, weight)
```

The two 5-arg overloads exist for convenience but each silently hardcodes one field and auto-prefixes the name key — mixing them with the 6-arg constructor (which does *not* auto-prefix) in the same bootstrap can produce mismatched translation keys if you're not careful. Craftorio's own `CraftorioShopBootstrap` only ever uses the 6-arg constructor, passing the `"registry."` prefix manually.

## The `unobtainable` flag

An effect marked `unobtainable = true` can **never** be granted by the random-effect timer or by rolling an Effect Rune. The filter lives in `CraftorioMisc.getRandomEffect`:

```java
public static CraftorioEffects getRandomEffect(RegistryAccess registryAccess, RandomSource random) {
    List<Holder.Reference<CraftorioEffects>> obtainable = getAllEffects(registryAccess).stream()
            .filter(holder -> !holder.value().isUnobtainable())
            .toList();
    return pickWeighted(obtainable, random, CraftorioEffects::getWeight).value().copy();
}
```

Use `unobtainable = true` for anything meant only as a deliberate consequence — a contract punishment, or a hand-placed reward — never something you'd want a player to get from a lucky roll. All of Craftorio's own punishment effects (`inquisitors_wrath`, `trazyns_curse`, `commeupance_of_the_gods`, `kingdom_tariff`, `copper_deficiency`) are `unobtainable = true` for exactly this reason: they're granted directly by resource-location lookup when a contract fails, completely bypassing the weighted-random pool.

`weight` only matters for effects that *aren't* unobtainable — it's the relative chance of that effect being picked whenever a random roll happens (the random-effect timer, or an Effect Rune's `randomEffectCount` roll).

## The bootstraps, and how they function

Craftorio splits its effect registrations across three bootstrap classes by type — this is purely organizational, all three register into the same `CraftorioEffects.REGISTRY_KEY`:

```java
public static void buffBootstrap(BootstrapContext<CraftorioEffects> context) {
    context.register(key("shop/0_25_increase"), new ShopMultiplierEffect(1.25F, "registry.0_25_increase", 25, DEFAULT_ICON, 10, true));
}

public static void debuffBootstrap(BootstrapContext<CraftorioEffects> context) {
    context.register(key("shop/kingdom_tariff"), new ShopMultiplierEffect(-1.25F, "registry.kingdom_tariff", 1200, DEFAULT_ICON, 10, true));
}
```

Just like contracts, `context.register(key, instance)` is the entire job of a bootstrap — it's a JSON generator that runs once during `runData`, and the actual runtime registry is populated purely from the resulting JSON files (`data/<namespace>/craftorio/effect/<id>.json`). All of Craftorio's effect bootstraps are wired into one `RegistrySetBuilder.add` call in `CraftorioDatagen.java`:

```java
.add(CraftorioEffects.REGISTRY_KEY, context -> {
    CraftorioTagEffectBootstrap.buffBootstrap(context);
    CraftorioTagEffectBootstrap.debuffBootstrap(context);
    CraftorioGeneralEffectBootstrap.buffBootstrap(context);
    CraftorioGeneralEffectBootstrap.debuffBootstrap(context);
    CraftorioShopBootstrap.buffBootstrap(context);
    CraftorioShopBootstrap.debuffBootstrap(context);
})
```

You can call as many bootstrap methods as you like inside one `.add(...)` lambda; the split into buff/debuff/general/tag/shop classes is just a convention, not a requirement.

## Adding your own effects from this addon

```java
public class MyEffectBootstrap {
    public static void bootstrap(BootstrapContext<CraftorioEffects> context) {
        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, Craftoriotemplate.prefix("my_buff")),
                new GeneralMultiplierEffect(2.0F, "registry.my_buff", 60, myIcon, 10, false)
        );
    }
}
```

Wire it in your own `GatherDataEvent` handler the same way as contracts:

```java
RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
        .add(CraftorioEffects.REGISTRY_KEY, MyEffectBootstrap::bootstrap);

event.getGenerator().addProvider(event.includeServer(),
        new DatapackBuiltinEntriesProvider(output, provider, registryBuilder, Set.of(Craftoriotemplate.MODID)));
```

This produces `data/craftoriotemplate/craftorio/effect/my_buff.json`, loaded alongside Craftorio's own effects. Since `weight` and `unobtainable` are ordinary fields in the JSON, a server admin can also just as easily hand-write a matching JSON file with no addon mod involved at all.

## What datagen actually produces

Craftorio's `general/multiplier_5` (a plain `GeneralMultiplierEffect`, `unobtainable` omitted since it's `false`, the codec's default):

```json
{
  "type": "craftorio:general_multiplier",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "multiplier": 5.0,
  "name": "registry.general_multiplier_5",
  "seconds": 30,
  "weight": 10
}
```

`tag/copper_block_buff` (a `TagMultiplierEffect` — note the extra `item_tag` field, serialized with the `#` tag prefix, and `unobtainable: true` explicitly written since it differs from the default):

```json
{
  "type": "craftorio:tag_multiplier",
  "icon": "craftorio:textures/gui/locked.png",
  "item_tag": "#craftorio:copper",
  "multiplier": 100.0,
  "name": "registry.copper_block_buff",
  "seconds": 25,
  "unobtainable": true,
  "weight": 10
}
```

`shop/kingdom_tariff` (a `ShopMultiplierEffect`, structurally identical to `GeneralMultiplierEffect`'s JSON apart from the `type`):

```json
{
  "type": "craftorio:shop_multiplier",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "multiplier": -1.25,
  "name": "registry.kingdom_tariff",
  "seconds": 1200,
  "unobtainable": true,
  "weight": 10
}
```

The `"type"` field is what the dispatch codec (`CraftorioEffects.dispatchCodec()`) uses to pick which `MapCodec` — and therefore which Java class — to deserialize the rest of the object with. It always matches whatever id you gave that type in `CraftorioEffectTypes` (`general_multiplier`, `tag_multiplier`, `shop_multiplier`), not the effect *instance's* own registry id.
