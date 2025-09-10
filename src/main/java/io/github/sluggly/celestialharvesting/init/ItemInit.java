package io.github.sluggly.celestialharvesting.init;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CelestialHarvesting.MOD_ID);

}
