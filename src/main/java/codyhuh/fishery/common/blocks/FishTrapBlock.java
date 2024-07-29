package codyhuh.fishery.common.blocks;

import codyhuh.fishery.common.blockentities.FishTrapBlockEntity;
import codyhuh.fishery.registry.ModBlockEntities;
import codyhuh.fishery.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FishTrapBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty OILED = BooleanProperty.create("oiled");

    public FishTrapBlock(Properties p_49224_) {
        super(p_49224_);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(OILED, false));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153213_, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.FISH_TRAP.get(), FishTrapBlockEntity::serverTick);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_51454_) {
        FluidState fluidstate = p_51454_.getLevel().getFluidState(p_51454_.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return super.getStateForPlacement(p_51454_).setValue(WATERLOGGED, flag).setValue(OILED, false);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51468_) {
        p_51468_.add(WATERLOGGED).add(OILED);
    }

    public FluidState getFluidState(BlockState p_51475_) {
        return p_51475_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_51475_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.FISH_TRAP.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        if (stack.is(ModItems.FISH_OIL.get()) && !pState.getValue(OILED)) {
            pLevel.setBlock(pPos, pState.setValue(OILED, true), -1);

            if (!pPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }

            pPlayer.swing(pHand);
            pLevel.playSound(null, pPos, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (stack.isEmpty() && pLevel.getBlockEntity(pPos) instanceof FishTrapBlockEntity blockEntity) {
            int itemCount = blockEntity.countItems(blockEntity.getItems());

            if (itemCount > 0) {
                pLevel.playSound(pPlayer, pPos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 0.35F, 1.0F);
                NonNullList<ItemStack> items = blockEntity.getItems();

                for (ItemStack itemStack : items) {
                    if (!pPlayer.getInventory().add(itemStack)) {
                        ItemEntity item = new ItemEntity(pLevel, pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, itemStack);
                        pLevel.addFreshEntity(item);
                    }
                    blockEntity.removeItem(items.size(), 1);
                }

            }
            return InteractionResult.SUCCESS;
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {

        if (state.getValue(OILED) && state.getValue(WATERLOGGED)) {
            for (int i = 0; i < 5; i++) {
                level.addParticle(new DustParticleOptions(Vec3.fromRGB24(0xaf8f2c).toVector3f(), 1.0F), pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble(), 0.0D, -0.5D, 0.0D);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = worldIn.getBlockEntity(pos);
            if (be instanceof Container) {
                Containers.dropContents(worldIn, pos, (Container)be);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
}
