package org.crimsoncrips.craftoriotemplate.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;

import java.util.concurrent.CompletableFuture;

public class CraftorioTemplatePointsProvider extends DataMapProvider {

    public CraftorioTemplatePointsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<String, Item> pointValue = this.builder(CraftorioDataMaps.POINT_VALUE);

        pointValue.add(ResourceLocation.fromNamespaceAndPath("create", "brass_ingot"), "300", false);

        pointValue.add(Items.DIAMOND.builtInRegistryHolder(), "9999", false);
    }

    @Override
    public String getName() {
        return "CraftorioTemplate Data Maps";
    }
}
