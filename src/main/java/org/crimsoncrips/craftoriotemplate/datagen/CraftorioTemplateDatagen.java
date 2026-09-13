package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CraftorioTemplateDatagen {

    public static void generateData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new CraftorioTemplatePointsProvider(output, provider));

        RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
                .add(CraftorioContract.REGISTRY_KEY, CraftorioTemplateContractBootstrap::bootstrap)
                .add(CraftorioEffects.REGISTRY_KEY, CraftorioTemplateEffectBootstrap::bootstrap)
                .add(CraftorioUpgrade.REGISTRY_KEY, CraftorioTemplateUpgradeBootstrap::bootstrap);

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, provider, registryBuilder, Set.of(Craftoriotemplate.MODID)));
    }
}
