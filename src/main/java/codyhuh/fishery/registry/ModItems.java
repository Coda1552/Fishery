package codyhuh.fishery.registry;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.common.items.HarpoonLauncherItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Fishery.MOD_ID);

    public static final RegistryObject<Item> HARPOON = ITEMS.register("harpoon_launcher", () -> new HarpoonLauncherItem(new Item.Properties().durability(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> FISH_OIL = ITEMS.register("fish_oil", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FISH_TRAP = ITEMS.register("fish_trap", () -> new BlockItem(ModBlocks.FISH_TRAP.get(), new Item.Properties()));
}
