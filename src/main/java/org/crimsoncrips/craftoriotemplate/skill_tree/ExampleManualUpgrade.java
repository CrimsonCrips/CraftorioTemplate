package org.crimsoncrips.craftoriotemplate.skill_tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;
import org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual.ActionUpgrade;

import java.math.BigInteger;

public class ExampleManualUpgrade extends ActionUpgrade {

    public static final MapCodec<ExampleManualUpgrade> CODEC = Common.CODEC.xmap(
            c -> {
                ExampleManualUpgrade upgrade = new ExampleManualUpgrade(c.name(), c.icon(), c.parent().orElse(null), c.description(), c.cost());
                upgrade.setPosition(c.x(), c.y());
                return upgrade;
            },
            u -> new Common(u.getNameKey(), u.getIcon(), u.getParent(), u.getDescriptionKey(), u.getCost(), u.getX(), u.getY())
    );

    public ExampleManualUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost);
    }

    public static ExampleManualUpgrade of(CraftorioUpgrade.Builder builder) {
        return new ExampleManualUpgrade(builder.getName(), builder.getIcon(), builder.getParent(), builder.getDescription(), builder.getCost());
    }

    @Override
    public void onUnlock(ServerPlayer player, ResourceLocation id) {
        for (ServerPlayer target : player.getServer().getPlayerList().getPlayers()) {
            target.sendSystemMessage(Component.literal("Hello"));
        }
    }

    @Override
    public MapCodec<? extends CraftorioUpgrade> codec() {
        return CraftorioTemplateUpgradeTypes.HELLO.get();
    }
}
