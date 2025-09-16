package io.github.sluggly.celestialharvesting.init;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.harvester.HarvesterBlock;
import io.github.sluggly.celestialharvesting.item.HarvesterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CelestialHarvesting.MOD_ID);

    public static final RegistryObject<Block> HARVESTER = registerBlock("harvester",
            () -> new HarvesterBlock(BlockBehaviour.Properties.of()
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
            ));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ItemInit.ITEMS.register(name, () -> new HarvesterBlockItem(block.get(), new Item.Properties()));
    }

}
