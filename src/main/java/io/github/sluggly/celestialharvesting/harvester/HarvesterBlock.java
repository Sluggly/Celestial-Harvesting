package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.init.BlockEntityInit;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HarvesterBlock extends BaseEntityBlock {

    public HarvesterBlock(Properties pProperties) { super(pProperties); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) { return new Harvester(pPos, pState); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntityInit.HARVESTER.get(), Harvester::tick);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof Harvester) {
                CompoundTag data = new CompoundTag();
                data.putLong(NBTKeys.BLOCK_POS, pPos.asLong());
                PacketHandler.sendToPlayer(NBTKeys.ACTION_OPEN_HARVESTER_SCREEN, data, (ServerPlayer) pPlayer);
            } else { throw new IllegalStateException("Our Harvester block entity is missing!"); }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }
}