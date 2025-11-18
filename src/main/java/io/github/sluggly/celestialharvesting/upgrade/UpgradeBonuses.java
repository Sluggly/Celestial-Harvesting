package io.github.sluggly.celestialharvesting.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public record UpgradeBonuses(
        Optional<Integer> grantsTier,
        Optional<Float> speed_modifier,
        Optional<Float> fuel_modifier,
        Optional<Float> damage_negation_chance,
        Optional<Integer> loot_rerolls,
        Optional<Integer> solar_generation,
        Optional<Integer> mission_bonus,
        Optional<Integer> inventory_rows,
        Optional<Integer> energy_capacity_bonus,
        Optional<Integer> unlocks_automation_tier
) {
    // This creates a codec that can be reused.
    public static final Codec<UpgradeBonuses> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("grantsTier").forGetter(UpgradeBonuses::grantsTier),
            Codec.FLOAT.optionalFieldOf("speed_modifier").forGetter(UpgradeBonuses::speed_modifier),
            Codec.FLOAT.optionalFieldOf("fuel_modifier").forGetter(UpgradeBonuses::fuel_modifier),
            Codec.FLOAT.optionalFieldOf("damage_negation_chance").forGetter(UpgradeBonuses::damage_negation_chance),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("loot_rerolls").forGetter(UpgradeBonuses::loot_rerolls),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("solar_generation").forGetter(UpgradeBonuses::solar_generation),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("mission_bonus").forGetter(UpgradeBonuses::mission_bonus),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("inventory_rows").forGetter(UpgradeBonuses::inventory_rows),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("energy_capacity_bonus").forGetter(UpgradeBonuses::energy_capacity_bonus),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("unlocks_automation_tier").forGetter(UpgradeBonuses::unlocks_automation_tier)
    ).apply(instance, UpgradeBonuses::new));
}
