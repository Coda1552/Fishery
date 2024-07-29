package codyhuh.fishery;

import codyhuh.fishery.registry.ModBlockEntities;
import codyhuh.fishery.registry.ModBlocks;
import codyhuh.fishery.registry.ModEntities;
import codyhuh.fishery.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Fishery.MOD_ID)
public class Fishery {
    public static final String MOD_ID = "fishery";

    public Fishery() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
    }
}
