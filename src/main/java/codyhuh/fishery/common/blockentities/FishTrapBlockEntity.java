package codyhuh.fishery.common.blockentities;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.common.blocks.FishTrapBlock;
import codyhuh.fishery.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

public class FishTrapBlockEntity extends BaseContainerBlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public FishTrapBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModBlockEntities.FISH_TRAP.get(), p_155229_, p_155230_);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container." + Fishery.MOD_ID + ".fish_trap");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FishTrapBlockEntity blockEntity) {
        int itemCount = blockEntity.countItems(blockEntity.getItems());

        if (level instanceof ServerLevel serverLevel && state.getValue(FishTrapBlock.OILED) && level.random.nextFloat() > 0.99F && state.getValue(FishTrapBlock.WATERLOGGED) && itemCount < blockEntity.getContainerSize()) {
            LootParams params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, pos.getCenter()).create(LootContextParamSet.builder().build());
            LootTable table = level.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING_FISH);
            List<ItemStack> list = table.getRandomItems(params);

            blockEntity.setItem(itemCount, list.get(0));

            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

    }

    @Override
    protected AbstractContainerMenu createMenu(int p_58627_, Inventory p_58628_) {
        return null;
    }

    @Override
    public boolean canOpen(Player p_58645_) {
        return false;
    }

    public int countItems(List<ItemStack> stacks) {
        List<ItemStack> currentStacks = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                currentStacks.add(stack);
            }
        }

        return currentStacks.size();
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        return ContainerHelper.removeItem(this.items, p_18942_, p_18943_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return ContainerHelper.takeItem(this.items, p_18951_);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }
}
