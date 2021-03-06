package eutros.metabotany.client.render;

import eutros.metabotany.client.render.model.FloatingFlowerModel;
import eutros.metabotany.common.block.flower.functional.SubtileBouganvillea;
import eutros.metabotany.common.block.tile.TileChargingPlate;
import eutros.metabotany.common.block.tile.TileSparkPainter;
import eutros.metabotany.common.block.tinkerer.tile.TileFrameTinkerer;
import eutros.metabotany.common.utils.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.client.render.tile.RenderTileFloatingFlower;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CustomRenderHandler {

    private CustomRenderHandler() {
    }

    @SuppressWarnings("unchecked") // IntelliJ more like DumbJ
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        ModelLoaderRegistry.registerLoader(FloatingFlowerModel.Loader.ID, FloatingFlowerModel.Loader.INSTANCE);

        ClientRegistry.bindTileEntityRenderer(SubtileBouganvillea.TYPE,
                RenderTileComposite.of(
                        RenderTileFloatingFlower::new,
                        RenderTileBouganvillea::new
                )
        );

        ClientRegistry.bindTileEntityRenderer(TileChargingPlate.TYPE, RenderTileChargingPlate::new);
        ClientRegistry.bindTileEntityRenderer(TileFrameTinkerer.TYPE, RenderTileFrameTinkerer::new);
        ClientRegistry.bindTileEntityRenderer(TileSparkPainter.TYPE, RenderTileSparkPainter::new);
    }

}