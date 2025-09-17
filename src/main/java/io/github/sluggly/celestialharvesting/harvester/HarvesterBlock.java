package io.github.sluggly.celestialharvesting.harvester;

import io.github.sluggly.celestialharvesting.init.BlockEntityInit;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HarvesterBlock extends BaseEntityBlock {

    public enum State implements StringRepresentable {
        IDLE("idle"),
        IN_MISSION("in_mission");

        private final String name;

        State(String name) { this.name = name; }

        @Override
        public @NotNull String getSerializedName() { return this.name; }
    }
    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

    public static final BooleanProperty UPGRADED = BooleanProperty.create("upgraded");
    protected static final VoxelShape SLAB_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape CARPET_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public HarvesterBlock(Properties pProperties) {
        super(pProperties.pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, State.IDLE).setValue(UPGRADED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) { pBuilder.add(STATE, UPGRADED); }

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
            if (entity instanceof Harvester harvester) {
                if (harvester.getHarvesterData().getStatus().equals(NBTKeys.HARVESTER_ONGOING)) {
                    pPlayer.sendSystemMessage(Component.literal("Harvester is currently on a mission."));
                    return InteractionResult.SUCCESS;
                }
                CompoundTag data = new CompoundTag();
                data.putLong(NBTKeys.BLOCK_POS, pPos.asLong());
                PacketHandler.sendToPlayer(NBTKeys.ACTION_OPEN_HARVESTER_SCREEN, data, (ServerPlayer) pPlayer);
            } else { throw new IllegalStateException("Our Harvester block entity is missing!"); }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {

        if (pState.getValue(STATE) == State.IN_MISSION) { return CARPET_SHAPE; }

        if (pState.getValue(UPGRADED)) { return Shapes.block(); }
        else { return SLAB_SHAPE; }
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(BlockState pState, @NotNull Player pPlayer, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        if (pState.getValue(STATE) == State.IN_MISSION) { return 0.0F; }
        return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        if (state.getValue(STATE) == State.IN_MISSION) { return 3600000.0F; }
        return super.getExplosionResistance(state, world, pos, explosion);
    }
}