package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.datagen.tags.CraftorioItemTagGen;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.ShopMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;

public class CraftorioTemplateEffectBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioEffects> context) {
        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_general_effect")),
                new GeneralMultiplierEffect(1.5F, "registry.craftoriotemplate_example_general_effect", 45, DEFAULT_ICON, 10, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_shop_effect")),
                new ShopMultiplierEffect(1.25F, "registry.craftoriotemplate_example_shop_effect", 45, DEFAULT_ICON, 10, false)
        );

        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_tag_effect")),
                new TagMultiplierEffect(50F, "registry.craftoriotemplate_example_tag_effect", CraftorioItemTagGen.COPPER, 45, DEFAULT_ICON, 10, false)
        );
    }
}
