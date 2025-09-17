package io.github.sluggly.celestialharvesting.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sluggly.celestialharvesting.mission.MissionItem;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record UpgradeDefinition(
        String name,
        int row,
        int column,
        String description,
        Item icon,
        List<MissionItem> cost,
        List<String> requirements,
        Optional<Integer> grantsTier
) {
    public static final Codec<UpgradeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(UpgradeDefinition::name),
            Codec.INT.fieldOf("row").forGetter(UpgradeDefinition::row),
            Codec.INT.fieldOf("column").forGetter(UpgradeDefinition::column),
            Codec.STRING.fieldOf("description").forGetter(UpgradeDefinition::description),
            ForgeRegistries.ITEMS.getCodec().fieldOf("icon").forGetter(UpgradeDefinition::icon),
            MissionItem.CODEC.listOf().fieldOf("cost").forGetter(UpgradeDefinition::cost),
            Codec.STRING.listOf().optionalFieldOf("requirements", List.of()).forGetter(UpgradeDefinition::requirements),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("grantsTier").forGetter(UpgradeDefinition::grantsTier)
    ).apply(instance, UpgradeDefinition::new));
}
