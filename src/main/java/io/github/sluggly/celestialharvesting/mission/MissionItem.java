package io.github.sluggly.celestialharvesting.mission;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public record MissionItem(Item item, int count, Optional<Double> chance) {
    public static final Codec<MissionItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(MissionItem::item),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(MissionItem::count),
            Codec.DOUBLE.optionalFieldOf("chance").forGetter(MissionItem::chance)
    ).apply(instance, MissionItem::new));

    public ItemStack toItemStack() { return new ItemStack(this.item, this.count); }
}