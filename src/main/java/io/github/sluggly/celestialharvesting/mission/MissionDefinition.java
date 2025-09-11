package io.github.sluggly.celestialharvesting.mission;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record MissionDefinition(
        String name,
        String icon,
        int travel,
        int fuel,
        Optional<List<String>> module,
        int damage,
        List<MissionItem> rewards
) {
    public static final Codec<MissionDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(MissionDefinition::name),
            Codec.STRING.fieldOf("icon").forGetter(MissionDefinition::icon),
            Codec.INT.fieldOf("travel").forGetter(MissionDefinition::travel),
            Codec.INT.fieldOf("fuel").forGetter(MissionDefinition::fuel),
            Codec.STRING.listOf().optionalFieldOf("module").forGetter(MissionDefinition::module),
            Codec.INT.fieldOf("damage").forGetter(MissionDefinition::damage),
            MissionItem.CODEC.listOf().fieldOf("rewards").forGetter(MissionDefinition::rewards)
    ).apply(instance, MissionDefinition::new));
}