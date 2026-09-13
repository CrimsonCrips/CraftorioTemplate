package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;

public class CraftorioTemplateEffectBootstrap {

    private static final ResourceLocation DEFAULT_ICON = Craftorio.getGuiTexture("default_contract_icon.png");

    public static void bootstrap(BootstrapContext<CraftorioEffects> context) {
        context.register(
                ResourceKey.create(CraftorioEffects.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Craftoriotemplate.MODID, "example_buff")),
                new GeneralMultiplierEffect(1.5F, "registry.craftoriotemplate_example_buff", 45, DEFAULT_ICON, 10, false)
        );
    }
}
