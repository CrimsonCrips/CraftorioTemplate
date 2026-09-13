package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.ModifierTarget;
import org.crimsoncrips.craftorio.skill_tree.UpgradeOperation;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.datagen.CraftorioModifierUpgrade;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;

public class CraftorioTemplateUpgradeBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioUpgrade> context) {
        ResourceKey<CraftorioUpgrade> craftorioRoot = ResourceKey.create(CraftorioUpgrade.REGISTRY_KEY, Craftorio.prefix("root"));

        CraftorioUpgrade.builder()
                .name("misc.craftoriotemplate.upgrade_example_upgrade")
                .icon(DEFAULT_ICON)
                .parent(craftorioRoot.location())
                .description("misc.craftoriotemplate.upgrade_example_upgrade_description")
                .cost(5000)
                .save(context, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_upgrade"),
                        b -> CraftorioModifierUpgrade.of(b, ModifierTarget.MULTIPLIER, UpgradeOperation.ADD, 0.15));
    }
}
