package io.github.sluggly.celestialharvesting.item;

import io.github.sluggly.celestialharvesting.client.renderer.HarvesterItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class HarvesterBlockItem extends BlockItem {
    public HarvesterBlockItem(Block pBlock, Properties pProperties) { super(pBlock, pProperties); }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new HarvesterItemRenderer();
            }
        });
    }
}

  