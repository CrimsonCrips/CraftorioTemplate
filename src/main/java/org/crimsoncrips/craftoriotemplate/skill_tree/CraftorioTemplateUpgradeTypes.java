package org.crimsoncrips.craftoriotemplate.skill_tree;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftoriotemplate.Craftoriotemplate;

import java.util.function.Supplier;

public class CraftorioTemplateUpgradeTypes {

    public static final DeferredRegister<MapCodec<? extends CraftorioUpgrade>> TYPES =
            DeferredRegister.create(CraftorioUpgrade.TYPE_REGISTRY_KEY, Craftoriotemplate.MODID);

    public static final Supplier<MapCodec<ExampleManualUpgrade>> HELLO =
            TYPES.register("hello", () -> ExampleManualUpgrade.CODEC);
}
