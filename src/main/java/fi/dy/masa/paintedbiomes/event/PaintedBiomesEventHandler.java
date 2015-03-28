package fi.dy.masa.paintedbiomes.event;

import net.minecraftforge.event.terraingen.WorldTypeEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.world.GenLayerBiomeGeneration;
import fi.dy.masa.paintedbiomes.world.GenLayerBiomeIndex;

public class PaintedBiomesEventHandler
{
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            ImageHandler.tickTimeouts();
        }
    }

    /*@SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        //ImageHandler.setTemplateBasePath(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSaveHandler().getWorldDirectory(), Reference.MOD_ID).getAbsolutePath());

        if (event.world.isRemote == false)
        {
            // Re-initialize the ImageHandler after a world loads, to update the possibly changed configs and template images
            ImageHandler.getImageHandler(event.world.provider.dimensionId).init();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (event.world.isRemote == false)
        {
            ImageHandler.removeImageHandler(event.world.provider.dimensionId);
        }
    }*/

    @SubscribeEvent
    public void onInitBiomeGen(WorldTypeEvent.InitBiomeGens event)
    {
        //PaintedBiomes.logger.info("InitBiomeGensEvent");
        ImageHandler.getImageHandler(0).init();
        event.newBiomeGens[0] = new GenLayerBiomeGeneration(event.seed, event.originalBiomeGens[0], event.worldType);
        event.newBiomeGens[1] = new GenLayerBiomeIndex(event.seed, event.originalBiomeGens[1], event.worldType);
        event.newBiomeGens[2] = event.newBiomeGens[0];
    }
}
