package fi.dy.masa.paintedbiomes.event;

import net.minecraftforge.event.terraingen.WorldTypeEvent.BiomeSize;
import net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.paintedbiomes.PaintedBiomes;

public class BiomeEvents
{
    @SubscribeEvent
    public void onInitBiomeGens(InitBiomeGens event)
    {
        PaintedBiomes.logger.info("InitBiomeGens: seed: " + event.seed);

        if (event.worldType != null)
        {
            PaintedBiomes.logger.info("InitBiomeGens: worldType: " + event.worldType.toString());
            PaintedBiomes.logger.info(String.format("worldTypeID: %d; worldTypeName: %s", event.worldType.getWorldTypeID(), event.worldType.getWorldTypeName()));
        }
        else
        {
            PaintedBiomes.logger.info("InitBiomeGens: worldType: null");
        }

        if (event.originalBiomeGens != null)
        {
            PaintedBiomes.logger.info("InitBiomeGens: event.originalBiomeGens.length: " + event.originalBiomeGens.length);
        }
        else
        {
            PaintedBiomes.logger.info("InitBiomeGens: event.originalBiomeGens: null");
        }
    }

    @SubscribeEvent
    public void onBiomeSize(BiomeSize event)
    {
        PaintedBiomes.logger.info("BiomeSize: size: " + event.originalSize);
    }
}
