# Making an Upgrade

An upgrade is one entry in the `craftorio:upgrade` datapack registry (`CraftorioUpgrade.REGISTRY_KEY`) — a node in the skill tree the player unlocks with points. There are two families: **datagen** upgrade types, which apply a declared numeric value automatically, and **manual** upgrade types, which run your own code once when unlocked.

## The upgrade *type* registry

Before an upgrade *instance* can be registered, its Java *class* needs a `MapCodec` entry in `CraftorioUpgradeTypes`:

```java
public static final Supplier<MapCodec<HealthUpgrade>> HEALTH =
        TYPES.register("health", () -> HealthUpgrade.CODEC);
```

Every upgrade class's `codec()` method points back at its own entry here. This is a separate concern from registering an actual upgrade *instance* in a bootstrap — think of `CraftorioUpgradeTypes` as "what kinds of upgrades exist" and the bootstrap as "which specific upgrades are placed in the tree." **A new upgrade Java class always needs exactly one line added here**; a new upgrade *instance* of an existing class does not.

## Datagen upgrades — declare a value, get automatic behavior

### `CraftorioModifierUpgrade` — the generic case

Most upgrades just add or multiply a number into one of Craftorio's existing formulas. `ModifierTarget` is the list of formulas that read these upgrades; `UpgradeOperation` is `ADD` or `MULTIPLY`:

```java
CraftorioModifierUpgrade.of(builder, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.1)
```

This upgrade doesn't do anything on unlock by itself — instead, every place in Craftorio that cares about a `ModifierTarget` calls `CraftorioMisc.getUpgradeModifierSum(player, target, operation)`, which walks every upgrade the player has unlocked, sums up every `CraftorioModifierUpgrade` matching that target/operation, and folds the result in at the point of use:

```java
multiplier += getUpgradeModifierSum(player, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD);
float multiplyFactor = (float) (1.0 + getUpgradeModifierSum(player, ModifierTarget.MULTIPLIER, UpgradeOperation.MULTIPLY));
```

**If your addon needs a brand-new formula that upgrades should be able to modify** (not one of the existing `ModifierTarget` values), you can't add to Craftorio's enum from outside — but you can add your own custom `ModifierTarget`-shaped system entirely in your own addon: define your own enum, your own sum helper mirroring `getUpgradeModifierSum`, and register `CraftorioModifierUpgrade` instances against Craftorio's *existing* enum only where you want to tap into a formula Craftorio itself already reads.

### `CraftorioAttributeUpgrade` — vanilla attribute upgrades

For upgrades that modify a vanilla `Attribute` (max health, movement speed, attack damage, etc.), extend `CraftorioAttributeUpgrade` instead — it applies a permanent `AttributeModifier` automatically on unlock:

```java
@Override
public void onUnlock(ServerPlayer player, ResourceLocation id) {
    AttributeInstance instance = player.getAttribute(getAttribute());
    if (instance == null) return;
    AttributeModifier.Operation op = operation == UpgradeOperation.ADD
            ? AttributeModifier.Operation.ADD_VALUE
            : AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    instance.addOrReplacePermanentModifier(new AttributeModifier(id, value, op));
}
```

The only thing a concrete subclass needs to provide is which attribute it targets. Here's the entire `HealthUpgrade` class — this is the complete boilerplate for a new attribute-based upgrade type:

```java
public class HealthUpgrade extends CraftorioAttributeUpgrade {

    public static final MapCodec<HealthUpgrade> CODEC = Common.CODEC.xmap(
            c -> new HealthUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost(), c.operation(), c.value()),
            h -> new Common(h.getNameKey(), h.getIcon(), h.getParent(), h.getDescriptionKey(), h.getCost(), h.getOperation(), h.getValue())
    );

    public HealthUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost, UpgradeOperation operation, double value) {
        super(name, icon, parent, description, cost, operation, value);
    }

    public static HealthUpgrade of(CraftorioUpgrade.Builder builder, UpgradeOperation operation, double value) {
        return of(builder, operation, value, HealthUpgrade::new);
    }

    @Override
    protected Holder<Attribute> getAttribute() { return Attributes.MAX_HEALTH; }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() { return CraftorioUpgradeTypes.HEALTH.get(); }
}
```

To add a new attribute-based upgrade type of your own (say, a knockback-resistance upgrade), copy this pattern: override `getAttribute()` with `Attributes.KNOCKBACK_RESISTANCE`, point `codec()` at a new `CraftorioUpgradeTypes` entry you add, done.

## Manual upgrades — `ActionUpgrade`

For anything that isn't "add/multiply a declared number," extend `ActionUpgrade` and override `activateFunction()`:

```java
public abstract class ActionUpgrade extends CraftorioUpgrade {
    public void activateFunction() { }

    @Override
    public void onUnlock(ServerPlayer player, ResourceLocation id) {
        activateFunction();
    }
}
```

`AdvancementMultiplierUpgrade` is a real example that does real work in `activateFunction()`-equivalent code — on unlock it retroactively scans every advancement the player has already completed and grants the matching multiplier bonus for each one, a one-time bulk effect that wouldn't make sense as a simple declared value:

```java
@Override
public void onUnlock(ServerPlayer player, ResourceLocation id) {
    activateFunction();
    for (AdvancementHolder advancement : player.server.getAdvancements().getAllAdvancements()) {
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) continue;
        double multiplierBonus = CraftorioAdvancementMultipliers.getMultiplier(advancement.id().toString());
        CraftorioMisc.addAdvancementMultiplierBonus(player, multiplierBonus);
    }
}
```

Other `ActionUpgrade` subclasses (like `SinkValueScalingUpgrade`) are pure **unlock gates** with no `onUnlock` side effect at all — the corresponding feature elsewhere in the codebase checks `CraftorioMisc.hasUnlockedUpgrade(player, Craftorio.prefix("sink_value_scaling"))` directly by id before applying its bonus, rather than the upgrade doing anything itself on unlock. Either style is valid: do the work in `activateFunction()`/`onUnlock`, or just check the id later wherever the feature lives.

The boilerplate for a plain gate-style `ActionUpgrade` is minimal — no extra constructor parameters needed since `ActionUpgrade` carries no fields of its own beyond the base `CraftorioUpgrade` ones:

```java
public class MyActionUpgrade extends ActionUpgrade {
    public static final MapCodec<MyActionUpgrade> CODEC = Common.CODEC.xmap(
            c -> new MyActionUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost()),
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost())
    );

    public MyActionUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static MyActionUpgrade of(CraftorioUpgrade.Builder builder) {
        return new MyActionUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return MyUpgradeTypes.MY_ACTION.get();
    }
}
```

## The bootstrap, and how it functions

An upgrade bootstrap uses `CraftorioUpgrade.builder()` rather than a raw constructor, because the builder is what lets you chain a `.parent(...)` reference:

```java
public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
    Holder.Reference<CraftorioUpgrade> root = CraftorioUpgrade.builder()
            .name("misc.craftorio.upgrade_root")
            .icon(DEFAULT_ICON)
            .description("misc.craftorio.upgrade_root_description")
            .cost(0)
            .save(context, id("root"), b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0));

    Holder.Reference<CraftorioUpgrade> health1 = CraftorioUpgrade.builder()
            .name("misc.craftorio.upgrade_health_1")
            .icon(DEFAULT_ICON)
            .parent(root)
            .description("misc.craftorio.upgrade_health_1_description")
            .cost(1000)
            .save(context, id("health_1"), b -> HealthUpgrade.of(b, UpgradeOperation.ADD, 2.0));

    CraftorioUpgrade.builder()
            .name("misc.craftorio.upgrade_health_2")
            .icon(DEFAULT_ICON)
            .parent(health1)
            .description("misc.craftorio.upgrade_health_2_description")
            .cost(10000)
            .save(context, id("health_2"), b -> HealthUpgrade.of(b, UpgradeOperation.ADD, 3.0));
}
```

`Builder.save(context, id, factory)` does two things: it calls `context.register(key, factory.apply(this))` (same `context.register` job as the other two registries), and it returns the `Holder.Reference` that was just registered — which you can then pass straight into `.parent(...)` for the next upgrade, chaining as deep a tree as you like (`root → health1 → health2` is a 2-deep chain; Craftorio itself goes 3 deep for `root → contractCompletionScaling → contractCompletionMultPerContract1`). An upgrade with no `.parent(...)` call becomes a root node of its own separate tree.

Manual (`ActionUpgrade`) upgrades slot into the same bootstrap identically — since their `of(Builder)` factory takes no extra arguments, you can pass the method reference directly:

```java
CraftorioUpgrade.builder()
        .name("misc.craftorio.upgrade_sink_value_scaling")
        .icon(DEFAULT_ICON)
        .parent(root)
        .description("misc.craftorio.upgrade_sink_value_scaling_description")
        .cost(10000)
        .save(context, id("sink_value_scaling"), SinkValueScalingUpgrade::of);
```

## How a new upgrade appears in the tree — automatically

The skill tree screen does not have a separate list of "known" upgrades to update. Every time it opens, it reads the *entire live registry* and lays itself out from scratch:

```java
Registry<CraftorioUpgrade> registry = this.minecraft.level.registryAccess().registryOrThrow(CraftorioUpgrade.REGISTRY_KEY);
Map<ResourceLocation, List<ResourceLocation>> children = new HashMap<>();
List<ResourceLocation> roots = new ArrayList<>();

for (Holder.Reference<CraftorioUpgrade> holder : registry.holders().toList()) {
    ResourceLocation id = holder.key().location();
    CraftorioUpgrade upgrade = holder.value();
    Optional<ResourceLocation> parent = upgrade.getParent();
    if (parent.isPresent()) {
        children.computeIfAbsent(parent.get(), k -> new ArrayList<>()).add(id);
    } else {
        roots.add(id);
    }
}
```

It groups every upgrade by its `getParent()`, finds the roots (upgrades with no parent), and recursively lays out each branch radially outward. **As long as your upgrade is registered with a `.parent(...)` pointing at any existing upgrade (Craftorio's or your own), it appears in the tree with no separate UI registration step.** Point it at Craftorio's own `root` (or any other node) to attach your addon's whole tree onto Craftorio's, or omit `.parent(...)` entirely to start a new, independent tree of your own.

## Unlocking — validated server-side

When a player clicks purchase, the client sends `UnlockUpgradePacket(upgradeId)`. The server handler re-validates everything rather than trusting the client:

```java
if (CraftorioMisc.hasUnlockedUpgrade(player, message.upgradeId)) return;

if (upgrade.getParent().isPresent() && !CraftorioMisc.hasUnlockedUpgrade(player, upgrade.getParent().get())) {
    player.sendSystemMessage(Component.translatable("misc.craftorio.upgrade_locked_tooltip").withStyle(ChatFormatting.RED));
    return;
}

BigInteger cost = upgrade.getCost();
if (CraftorioMisc.getPoints(player).compareTo(cost) < 0) {
    player.sendSystemMessage(Component.translatable("misc.craftorio.not_enough_points").withStyle(ChatFormatting.RED));
    return;
}

CraftorioMisc.setPoints(points.subtract(cost), player);
CraftorioMisc.unlockUpgrade(player, message.upgradeId);
upgrade.onUnlock(player, message.upgradeId);
```

This is why the parent-lock check matters even though the client UI already greys out locked nodes — a modified client can't skip ahead, since the parent's unlock state is re-checked here before `onUnlock` ever runs.

## Adding your own upgrades from this addon

```java
public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
    ResourceKey<CraftorioUpgrade> craftorioRoot = ResourceKey.create(CraftorioUpgrade.REGISTRY_KEY, Craftorio.prefix("root"));

    CraftorioUpgrade.builder()
            .name("misc.craftoriotemplate.upgrade_my_upgrade")
            .icon(myIcon)
            .parent(craftorioRoot)
            .description("misc.craftoriotemplate.upgrade_my_upgrade_description")
            .cost(5000)
            .save(context, ResourceKey.create(CraftorioUpgrade.REGISTRY_KEY, Craftoriotemplate.prefix("my_upgrade")),
                    b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.15));
}
```

Note `.parent(craftorioRoot)` here takes a plain `ResourceKey`/`ResourceLocation` (the other `.parent(...)` overload) rather than a `Holder.Reference`, since Craftorio's `root` upgrade was registered in a different bootstrap you don't have a `Holder.Reference` for — you just need its id. Wire this into your own `RegistrySetBuilder` the same way as the other two registries, and your upgrade appears attached directly onto Craftorio's own tree.

## What datagen actually produces

`health_1`, a `CraftorioAttributeUpgrade` subclass (`HealthUpgrade`) — note `type` names the upgrade *type* id (`craftorio:health`, from `CraftorioUpgradeTypes`), while `parent` names the *instance* id of another upgrade (`craftorio:root`):

```json
{
  "type": "craftorio:health",
  "parent": "craftorio:root",
  "cost": "1000",
  "description": "misc.craftorio.upgrade_health_1_description",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "name": "misc.craftorio.upgrade_health_1",
  "operation": "ADD",
  "value": 2.0
}
```

`multiplier_1`, a plain `CraftorioModifierUpgrade` — one extra field (`target`) compared to `HealthUpgrade`'s JSON, since the generic modifier type needs to say *which* formula it feeds into:

```json
{
  "type": "craftorio:modifier",
  "parent": "craftorio:root",
  "cost": "5000",
  "description": "misc.craftorio.upgrade_multiplier_1_description",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "name": "misc.craftorio.upgrade_multiplier_1",
  "operation": "ADD",
  "target": "MULTIPLIER",
  "value": 0.1
}
```

`sink_value_scaling`, an `ActionUpgrade` — no `operation`/`value`/`target` at all, since `ActionUpgrade.Common`'s codec only carries the five base fields every upgrade has:

```json
{
  "type": "craftorio:sink_value_scaling",
  "parent": "craftorio:root",
  "cost": "10000",
  "description": "misc.craftorio.upgrade_sink_value_scaling_description",
  "icon": "craftorio:textures/gui/default_contract_icon.png",
  "name": "misc.craftorio.upgrade_sink_value_scaling"
}
```

This is the clearest illustration of the datagen/manual split from earlier in this page: a `CraftorioModifierUpgrade` or `CraftorioAttributeUpgrade` JSON always carries the numeric payload that makes it self-sufficient at runtime, while an `ActionUpgrade` JSON is just an id, a cost, and a tree position — all of its actual behavior lives in Java code (`activateFunction()`, or a `hasUnlockedUpgrade` check elsewhere), not in the generated data.
