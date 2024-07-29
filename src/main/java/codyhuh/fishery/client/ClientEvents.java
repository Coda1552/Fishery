package codyhuh.fishery.client;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.client.render.HarpoonRenderer;
import codyhuh.fishery.client.render.blockentities.FishTrapBlockEntityRenderer;
import codyhuh.fishery.registry.ModBlockEntities;
import codyhuh.fishery.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Fishery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void clientSetup(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.HARPOON.get(), HarpoonRenderer::new);
        e.registerBlockEntityRenderer(ModBlockEntities.FISH_TRAP.get(), FishTrapBlockEntityRenderer::new);
    }
}
