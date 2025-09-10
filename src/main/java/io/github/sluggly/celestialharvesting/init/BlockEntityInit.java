package io.github.sluggly.celestialharvesting.init;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CelestialHarvesting.MOD_ID);

    public static final RegistryObject<BlockEntityType<Harvester>> HARVESTER = BLOCK_ENTITIES.register("spatial_harvester", () -> BlockEntityType.Builder.of(Harvester::new, BlockInit.HARVESTER.get()).build(null));

}
