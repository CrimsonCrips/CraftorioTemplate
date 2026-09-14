package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.skill_tree.AttributeTarget;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioAttributeUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;
import org.crimsoncrips.craftoriotemplate.skill_tree.ExampleManualUpgrade;

public class CraftorioTemplateUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        ResourceKey<CraftorioUpgrade> craftorioRoot = ResourceKey.create(CraftorioUpgrade.REGISTRY_KEY, Craftorio.prefix("root"));
        ResourceLocation rootLocation = craftorioRoot.location();

        Holder.Reference<CraftorioUpgrade> exampleUpgrade = CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_example_upgrade")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_example_upgrade_description")
                .cost(5000)
                .save(context, id("example_upgrade"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.15));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_hello")
                .icon(DEFAULT_ICON)
                .parent(exampleUpgrade)
                .description("misc.craftoriotemplate.upgrade_hello_description")
                .cost(1000)
                .save(context, id("hello"), ExampleManualUpgrade::of);

        // One CraftorioModifierUpgrade example per ModifierTarget, showing the generic mechanism used by every upgrade function.

        modifierExample(context, rootLocation, "multiplier", ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "item_base_value", ModifierTarget.ITEM_BASE_VALUE, UpgradeOperation.MULTIPLY, 0.1);
        modifierExample(context, rootLocation, "contract_refresh_speed", ModifierTarget.CONTRACT_REFRESH_SPEED, UpgradeOperation.MULTIPLY, 0.1);
        modifierExample(context, rootLocation, "effect_timer_speed", ModifierTarget.EFFECT_TIMER_SPEED, UpgradeOperation.MULTIPLY, 0.1);
        modifierExample(context, rootLocation, "punishment_duration", ModifierTarget.PUNISHMENT_DURATION, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "effect_duration", ModifierTarget.EFFECT_DURATION, UpgradeOperation.MULTIPLY, 0.1);
        modifierExample(context, rootLocation, "expansion_cost", ModifierTarget.EXPANSION_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "rarer_contract_chance", ModifierTarget.RARER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "rarer_effect_chance", ModifierTarget.RARER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "shop_cost", ModifierTarget.SHOP_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "contract_refresh_cost", ModifierTarget.CONTRACT_REFRESH_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "lost_bet_refund", ModifierTarget.LOST_BET_REFUND, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "mult_per_contract_done", ModifierTarget.MULT_PER_CONTRACT_DONE, UpgradeOperation.ADD, 0.001);
        modifierExample(context, rootLocation, "bet_odds", ModifierTarget.BET_ODDS, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "bet_bonus", ModifierTarget.BET_BONUS, UpgradeOperation.ADD, 0.5);

        // ITEM_TAG_BASE_VALUE needs an item tag on top of the target/operation/value, via the 5-arg CraftorioModifierUpgrade.of overload.
        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_modifier_target_item_tag_base_value")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_modifier_target_item_tag_base_value_description")
                .cost(100)
                .save(context, id("modifier_target_item_tag_base_value"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_TAG_BASE_VALUE, UpgradeOperation.MULTIPLY, 0.1, CraftorioItemTagGen.COPPER));

        // One CraftorioAttributeUpgrade example per AttributeTarget, showing the generic "apply a vanilla Attribute" mechanism.

        attributeExample(context, rootLocation, "health", AttributeTarget.HEALTH, UpgradeOperation.ADD, 2.0);
        attributeExample(context, rootLocation, "speed", AttributeTarget.SPEED, UpgradeOperation.ADD, 0.02);
        attributeExample(context, rootLocation, "defense", AttributeTarget.DEFENSE, UpgradeOperation.ADD, 1.0);
        attributeExample(context, rootLocation, "damage", AttributeTarget.DAMAGE, UpgradeOperation.ADD, 1.0);
        attributeExample(context, rootLocation, "block_reach", AttributeTarget.BLOCK_REACH, UpgradeOperation.ADD, 1.0);
        attributeExample(context, rootLocation, "jump_height", AttributeTarget.JUMP_HEIGHT, UpgradeOperation.ADD, 0.1);
        attributeExample(context, rootLocation, "xp_gain", AttributeTarget.XP_GAIN, UpgradeOperation.ADD, 0.1);
        attributeExample(context, rootLocation, "resistance", AttributeTarget.RESISTANCE, UpgradeOperation.ADD, 1.0);
    }

    private static void modifierExample(BootstrapContext<CraftorioUpgrade> context, ResourceLocation rootLocation, String idSuffix, ModifierTarget target, UpgradeOperation operation, double value) {
        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_modifier_target_" + idSuffix)
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_modifier_target_" + idSuffix + "_description")
                .cost(100)
                .save(context, id("modifier_target_" + idSuffix), b -> CraftorioModifierUpgrade.of(b, target, operation, value));
    }

    private static void attributeExample(BootstrapContext<CraftorioUpgrade> context, ResourceLocation rootLocation, String idSuffix, AttributeTarget target, UpgradeOperation operation, double value) {
        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_" + idSuffix)
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_" + idSuffix + "_description")
                .cost(100)
                .save(context, id("attribute_" + idSuffix), b -> CraftorioAttributeUpgrade.of(b, target, operation, value));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, path);
    }
}
