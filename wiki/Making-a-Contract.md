# Making a Contract

A contract is one entry in the `craftorio:contract` datapack registry (`CraftorioContract.REGISTRY_KEY`). It describes a timed bounty: the player hands in a list of required items, and on completion receives points, item rewards, and (on failure) an optional punishment effect.

## The pieces

### `CraftorioContractItem` — a single bounty line

Each bounty line requires an amount of *something*, and that something can be matched three different ways. Only one of the three is ever set on a given instance:

```java
public boolean matches(ItemStack stack) {
    if (stackNeeded.isPresent()) {
        return ItemStack.isSameItemSameComponents(stack, stackNeeded.get());
    }
    if (itemNeeded.isPresent()) {
        return itemNeeded.get().equals(stack.getItem());
    }
    return itemTag.isPresent() && stack.is(itemTag.get());
}
```

| Constructor | Matches by |
|---|---|
| `new CraftorioContractItem(amount, Item)` | item identity only — any stack of that item counts, regardless of data components |
| `new CraftorioContractItem(amount, TagKey<Item>)` | tag membership — any item in the tag counts |
| `new CraftorioContractItem(amount, ItemStack)` | `ItemStack.isSameItemSameComponents` — the stack's item **and every data component** must match |

The third form is how you require an item carrying specific data components — for example Craftorio's own Effect Rune (`org.crimsoncrips.craftorio.item.EffectRune`) stores its rolled effects on a custom data component, `CraftorioDataComponents.EFFECTS_STORED` (a `DataComponentType<List<CraftorioEffects>>`). To build a bounty line that only accepts a rune carrying one specific effect:

```java
ItemStack requiredRune = new ItemStack(CraftorioItems.EFFECT_RUNE.get());
requiredRune.set(CraftorioDataComponents.EFFECTS_STORED.get(), List.of(specificEffectInstance));

CraftorioContractItem line = new CraftorioContractItem(1, requiredRune);
```

`ItemStack.isSameItemSameComponents` compares every component on the stack, so a rune with any *other* rolled effect (or no `EFFECTS_STORED` component at all) will not satisfy this line. This same technique works for any custom data component your own addon registers on its own items — the contract system doesn't care where the component came from.

### `CraftorioContractItemReward` — what the player gets

```java
new CraftorioContractItemReward(amountGiving, rewardingItem)
new CraftorioContractItemReward(amountGiving, rewardingItem, randomEffectCount)
```

When `randomEffectCount` is `0` (the default), the reward is just `amountGiving` copies of the item. When it's greater than `0`, the reward system gives `amountGiving` **separate single-item stacks**, each independently rolling `randomEffectCount` random effects onto its own `EFFECTS_STORED` component:

```java
for (int i = 0; i < amountGiving; i++) {
    ItemStack stack = rewardingStack.copy();
    List<CraftorioEffects> rolled = new ArrayList<>();
    for (int j = 0; j < randomEffectCount; j++) {
        rolled.add(CraftorioMisc.getRandomEffect(player.registryAccess(), random));
    }
    stack.set(CraftorioDataComponents.EFFECTS_STORED.get(), rolled);
    ...
}
```

This is exactly how "5 Effect Runes" from a single contract each come out with a *different* rolled effect instead of five identical copies — each loop iteration rolls fresh. `CraftorioMisc.getRandomEffect` only picks from effects where `isUnobtainable()` is `false` (see [Making an Effect](Making-an-Effect.md)), so a reward roll can never hand out a punishment-only effect.

### `CraftorioContract` — the contract itself

The constructor used by bootstraps:

```java
new CraftorioContract(
    List<CraftorioContractItem> itemBounty,
    String langKey,                 // becomes "registry.<langKey>.title" / ".description"
    int seconds,                    // time limit
    BigInteger basePointValue,      // multiplied by the player's Craftorio multiplier on completion
    List<CraftorioContractItemReward> rewards,
    ResourceLocation icon,
    Optional<ResourceLocation> punishment,   // an effect id, granted on failure
    int weight,                     // roll weight when contracts are offered
    BigInteger pointThreshold,
    BigInteger minPointThreshold,   // player must have at least this many points to be offered the contract
    BigInteger maxPointThreshold,   // player must have at most this many points to be offered the contract
    Optional<String> requiredModId  // contract is skipped entirely if this mod isn't loaded
)
```

A real registration from Craftorio's own bootstrap:

```java
context.register(
        key("cake_delivery"), new CraftorioContract(
                List.of(
                        newContractItem(1, Items.CAKE)
                ), "cake_delivery", 120, BigInteger.valueOf(300),
                List.of(
                        newContractReward(1, Items.GOLD_INGOT)
                ),
                DEFAULT_ICON,
                Optional.empty(),
                10,
                BigInteger.ZERO,
                BigInteger.ZERO,
                scientificToInt("1e4"),
                Optional.empty()
        )
);
```

A punishment + Effect Rune reward example (the `hephaestus_prison` contract):

```java
context.register(
        key("hephaestus_prison"), new CraftorioContract(
                List.of(
                        newContractItem(94353, Items.LAVA_BUCKET),
                        newContractItem(1111, Items.WATER_BUCKET)
                        // ...over a hundred more bounty lines
                ), "hephaestus_prison", 43200, BigInteger.valueOf(500_000_000L),
                List.of(
                        newContractReward(5, CraftorioItems.EFFECT_RUNE.get(), 2),
                        newContractReward(3, CraftorioItems.MYSTERY_EFFECT_RUNE.get(), 2)
                ),
                DEFAULT_ICON,
                Optional.of(Craftorio.prefix("general/commeupance_of_the_gods")),
                10,
                scientificToInt("1e10"),
                scientificToInt("1e7"),
                scientificToInt("1e309"),
                Optional.empty()
        )
);
```

Note that this contract requires plain vanilla bucket *items* — matching is by item identity here, not data components. Use the `ItemStack` constructor shown above only when you actually need to distinguish stacks by their components.

## The bootstrap, and how it functions

A bootstrap is a static method matching the shape NeoForge's `RegistrySetBuilder` expects:

```java
public static void bootstrap(BootstrapContext<CraftorioContract> context) {
    context.register(key("cake_delivery"), new CraftorioContract(...));
    context.register(key("animal_feed"), new CraftorioContract(...));
    // ...
}
```

`context.register(ResourceKey<CraftorioContract>, CraftorioContract)` is the only thing a bootstrap actually does — it hands NeoForge an id and an instance. Craftorio's `key(String path)` helper is just `ResourceKey.create(CraftorioContract.REGISTRY_KEY, Craftorio.prefix(path))`.

`RegistrySetBuilder.add(CraftorioContract.REGISTRY_KEY, YourBootstrap::bootstrap)` runs your bootstrap **once, during `runData`**, and `DatapackBuiltinEntriesProvider` serializes every entry your bootstrap registered into JSON. At actual game runtime, no Java bootstrap code runs at all — the datapack registry is populated purely by reading that generated JSON (plus any hand-written JSON in `data/<namespace>/craftorio/contract/`) back in. This is why the registry is a *datapack* registry, not a static Java one: a server admin (or another datapack) can add, remove, or edit contracts with zero Java involved, and your addon's Java bootstrap is really just a JSON generator that runs at build time.

## Adding your own contracts from this addon

Create your own bootstrap class:

```java
public class MyContractBootstrap {
    public static void bootstrap(BootstrapContext<CraftorioContract> context) {
        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, Craftoriotemplate.prefix("my_contract")),
                new CraftorioContract(
                        List.of(new CraftorioContractItem(16, Items.IRON_INGOT)),
                        "my_contract", 600, BigInteger.valueOf(1000),
                        List.of(new CraftorioContractItemReward(1, Items.DIAMOND)),
                        Craftoriotemplate.prefix("textures/gui/my_icon.png"),
                        Optional.empty(),
                        10,
                        BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(1_000_000),
                        Optional.empty()
                )
        );
    }
}
```

Then wire it into your own `GatherDataEvent` handler, exactly the way Craftorio wires its own:

```java
public static void generateData(GatherDataEvent event) {
    PackOutput output = event.getGenerator().getPackOutput();
    CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

    RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
            .add(CraftorioContract.REGISTRY_KEY, MyContractBootstrap::bootstrap);

    event.getGenerator().addProvider(event.includeServer(),
            new DatapackBuiltinEntriesProvider(output, provider, registryBuilder, Set.of(Craftoriotemplate.MODID)));
}
```

Running `runData` now produces `data/craftoriotemplate/craftorio/contract/my_contract.json` alongside Craftorio's own `data/craftorio/craftorio/contract/*.json` files — both get picked up together at runtime, and your contract shows up in the game exactly like one of Craftorio's own.

## What datagen actually produces

Here's Craftorio's own `cake_delivery` contract's generated JSON, for the exact bootstrap call shown earlier in this page — this is the real file `runData` writes to `data/craftorio/craftorio/contract/cake_delivery.json`:

```json
{
  "basePointValue": "300",
  "description": "registry.cake_delivery.description",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "itemBounty": [
    {
      "amountRequired": 1,
      "contract_item": "minecraft:cake"
    }
  ],
  "itemRewards": [
    {
      "amountGiving": 1,
      "rewardingItem": {
        "count": 1,
        "id": "minecraft:gold_ingot"
      }
    }
  ],
  "max_point_threshold": "10000",
  "name": "registry.cake_delivery.title",
  "seconds": 120,
  "weight": 10
}
```

Notice `Optional.empty()` fields (`punishment`, `requiredModId`) are omitted entirely rather than written as `null`, and `pointThreshold`/`minPointThreshold` are likewise omitted here since they were passed as `BigInteger.ZERO` (the codec's default). Compare this to `hephaestus_prison`, which sets a punishment and gives Effect Rune rewards with `randomEffectCount`:

```json
{
  "basePointValue": "500000000",
  "description": "registry.hephaestus_prison.description",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "itemBounty": [
    { "amountRequired": 211179, "contract_item": "minecraft:andesite" },
    { "amountRequired": 87, "contract_item": "minecraft:anvil" }
  ],
  "itemRewards": [
    {
      "amountGiving": 5,
      "randomEffectCount": 2,
      "rewardingItem": { "count": 1, "id": "craftorio:effect_rune" }
    },
    {
      "amountGiving": 3,
      "randomEffectCount": 2,
      "rewardingItem": { "count": 1, "id": "craftorio:mystery_effect_rune" }
    }
  ],
  "min_point_threshold": "10000000",
  "name": "registry.hephaestus_prison.title",
  "point_threshold": "10000000000",
  "punishment": "craftorio:general/commeupance_of_the_gods",
  "seconds": 43200,
  "weight": 10
}
```

`randomEffectCount` sits right alongside `rewardingItem` and `amountGiving` — it's just the optional third field on `CraftorioContractItemReward.CODEC`.

If you register a bounty line using the data-component (`ItemStack`) constructor from earlier in this page — requiring a rune with one specific rolled effect — the bounty line serializes with a `contract_item_stack` field instead of `contract_item`, and the item's data components nest under `components`:

```json
{
  "amountRequired": 1,
  "contract_item_stack": {
    "id": "craftorio:effect_rune",
    "count": 1,
    "components": {
      "craftorio:effects_stored": [
        {
          "type": "craftorio:general_multiplier",
          "multiplier": 2.0,
          "name": "registry.my_effect",
          "seconds": 60,
          "weight": 10
        }
      ]
    }
  }
}
```

This is exactly why `ItemStack.isSameItemSameComponents` matching exists as a separate mode from plain `contract_item` — the JSON captures the full component state, not just the item id, so a rune with a *different* rolled effect (or no `effects_stored` component at all) fails to match this bounty line.
