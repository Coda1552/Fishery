package codyhuh.fishery.registry;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.common.blocks.FishTrapBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Fishery.MOD_ID);

    public static final RegistryObject<Block> FISH_TRAP = BLOCKS.register("fish_trap", () -> new FishTrapBlock(BlockBehaviour.Properties.of().randomTicks().noOcclusion().sound(SoundType.WOOL)));
}
