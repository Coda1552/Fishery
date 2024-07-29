package codyhuh.fishery.registry;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.common.entities.HarpoonProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Fishery.MOD_ID);

    public static final RegistryObject<EntityType<HarpoonProjectile>> HARPOON = ENTITY_TYPES.register("harpoon", () -> EntityType.Builder.<HarpoonProjectile>of(HarpoonProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).build("harpoon"));
}
