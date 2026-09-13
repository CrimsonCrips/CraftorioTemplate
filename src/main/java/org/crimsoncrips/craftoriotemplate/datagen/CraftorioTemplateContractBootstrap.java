package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioDataComponents;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItem;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContractItemReward;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class CraftorioTemplateContractBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioContract> context) {

        CraftorioEffects requiredRuneEffect = new GeneralMultiplierEffect(2.0F, "registry.craftoriotemplate_required_effect", 60, DEFAULT_ICON, 10, false);
        ItemStack requiredRune = new ItemStack(CraftorioItems.EFFECT_RUNE.get());
        requiredRune.set(CraftorioDataComponents.EFFECTS_STORED.get(), List.of(requiredRuneEffect));

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_data_component_contract")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(1, requiredRune)
                        ),
                        "example_data_component_contract", 600, BigInteger.valueOf(1000),
                        List.of(
                                new CraftorioContractItemReward(4, Items.DIAMOND)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        10,
                        BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(1_000_000),
                        Optional.empty()
                )
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_item_tag_contract")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(32, CraftorioItemTagGen.COPPER)
                        ),
                        "example_item_tag_contract", 600, BigInteger.valueOf(800),
                        List.of(
                                new CraftorioContractItemReward(2, Items.EMERALD)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        10,
                        BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(1_000_000),
                        Optional.empty()
                )
        );

        context.register(
                ResourceKey.create(CraftorioContract.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_item_list_contract")),
                new CraftorioContract(
                        List.of(
                                new CraftorioContractItem(16, Items.IRON_INGOT),
                                new CraftorioContractItem(8, Items.GOLD_INGOT),
                                new CraftorioContractItem(4, Items.DIAMOND)
                        ),
                        "example_item_list_contract", 600, BigInteger.valueOf(1200),
                        List.of(
                                new CraftorioContractItemReward(1, Items.NETHERITE_SCRAP)
                        ),
                        DEFAULT_ICON,
                        Optional.empty(),
                        10,
                        BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(1_000_000),
                        Optional.empty()
                )
        );
    }
}
