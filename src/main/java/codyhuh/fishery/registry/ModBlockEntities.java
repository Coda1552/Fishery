package codyhuh.fishery.registry;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.common.blockentities.FishTrapBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Fishery.MOD_ID);

    public static final RegistryObject<BlockEntityType<FishTrapBlockEntity>> FISH_TRAP = BLOCK_ENTITIES.register("fish_trap", () -> BlockEntityType.Builder.of(FishTrapBlockEntity::new, ModBlocks.FISH_TRAP.get()).build(null));
}
