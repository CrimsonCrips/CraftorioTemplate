package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.BlockReachUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.DamageUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.DefenseUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.HealthUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.JumpHeightUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.ResistanceUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.SpeedUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.XpGainUpgrade;
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
        modifierExample(context, rootLocation, "punishment_effect_duration", ModifierTarget.PUNISHMENT_EFFECT_DURATION, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "effect_duration", ModifierTarget.EFFECT_DURATION, UpgradeOperation.MULTIPLY, 0.1);
        modifierExample(context, rootLocation, "claim_chunk_cost", ModifierTarget.CLAIM_CHUNK_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "border_expansion_cost", ModifierTarget.BORDER_EXPANSION_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "better_contract_chance", ModifierTarget.BETTER_CONTRACT_CHANCE, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "better_effect_chance", ModifierTarget.BETTER_EFFECT_CHANCE, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "shop_cost", ModifierTarget.SHOP_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "contract_refresh_cost", ModifierTarget.CONTRACT_REFRESH_COST, UpgradeOperation.MULTIPLY, -0.1);
        modifierExample(context, rootLocation, "double_or_nothing_loss_refund", ModifierTarget.DOUBLE_OR_NOTHING_LOSS_REFUND, UpgradeOperation.ADD, 0.1);
        modifierExample(context, rootLocation, "contract_completion_mult_per_contract", ModifierTarget.CONTRACT_COMPLETION_MULT_PER_CONTRACT, UpgradeOperation.ADD, 0.001);

        // ITEM_TAG_BASE_VALUE needs an item tag on top of the target/operation/value, via the 5-arg CraftorioModifierUpgrade.of overload.
        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_modifier_target_item_tag_base_value")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_modifier_target_item_tag_base_value_description")
                .cost(100)
                .save(context, id("modifier_target_item_tag_base_value"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.ITEM_TAG_BASE_VALUE, UpgradeOperation.MULTIPLY, 0.1, CraftorioItemTagGen.COPPER));

        // One example per CraftorioAttributeUpgrade subclass, showing the "apply a vanilla Attribute" family of upgrade functions.

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_health")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_health_description")
                .cost(100)
                .save(context, id("attribute_health"), b -> HealthUpgrade.of(b, UpgradeOperation.ADD, 2.0));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_speed")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_speed_description")
                .cost(100)
                .save(context, id("attribute_speed"), b -> SpeedUpgrade.of(b, UpgradeOperation.ADD, 0.02));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_defense")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_defense_description")
                .cost(100)
                .save(context, id("attribute_defense"), b -> DefenseUpgrade.of(b, UpgradeOperation.ADD, 1.0));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_damage")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_damage_description")
                .cost(100)
                .save(context, id("attribute_damage"), b -> DamageUpgrade.of(b, UpgradeOperation.ADD, 1.0));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_block_reach")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_block_reach_description")
                .cost(100)
                .save(context, id("attribute_block_reach"), b -> BlockReachUpgrade.of(b, UpgradeOperation.ADD, 1.0));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_jump_height")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_jump_height_description")
                .cost(100)
                .save(context, id("attribute_jump_height"), b -> JumpHeightUpgrade.of(b, UpgradeOperation.ADD, 0.1));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_xp_gain")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_xp_gain_description")
                .cost(100)
                .save(context, id("attribute_xp_gain"), b -> XpGainUpgrade.of(b, UpgradeOperation.ADD, 0.1));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_attribute_resistance")
                .icon(DEFAULT_ICON)
                .parent(rootLocation)
                .description("misc.craftoriotemplate.upgrade_attribute_resistance_description")
                .cost(100)
                .save(context, id("attribute_resistance"), b -> ResistanceUpgrade.of(b, UpgradeOperation.ADD, 1.0));
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, path);
    }
}
