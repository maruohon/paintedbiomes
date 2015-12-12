package fi.dy.masa.paintedbiomes.event;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.world.GenLayerBiomeGeneration;
import fi.dy.masa.paintedbiomes.world.GenLayerBiomeIndex;
import fi.dy.masa.paintedbiomes.world.WorldChunkManagerPaintedBiomes;

public class PaintedBiomesEventHandler
{
    public PaintedBiomesEventHandler()
    {
        //ReferenceReflection.fieldWorldChunkProvider = ReflectionHelper.findField(World.class, "v", "field_73020_y", "chunkProvider");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            ImageHandler.tickTimeouts();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        overrideChunkProvider(event.world);
        overrideWorldChunkManager(event.world);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (Configs.getEffectiveMainConfig().useGenLayer == false)
        {
            ImageHandler.removeImageHandler(event.world.provider.dimensionId);
        }
    }

    @SubscribeEvent
    public void onInitBiomeGen(WorldTypeEvent.InitBiomeGens event)
    {
        if (Configs.getEffectiveMainConfig().useGenLayer == false)
        {
            return;
        }

        PaintedBiomes.logger.info("Registering Painted Biomes biome GenLayers...");
        ImageHandler.getImageHandler(0).init();
        event.newBiomeGens[0] = new GenLayerBiomeGeneration(event.seed, event.originalBiomeGens[0], event.worldType);
        event.newBiomeGens[1] = new GenLayerBiomeIndex(event.seed, event.originalBiomeGens[1], event.worldType);
        event.newBiomeGens[2] = event.newBiomeGens[0];
    }

    private static void overrideWorldChunkManager(World world)
    {
        if (Configs.getEffectiveMainConfig().useGenLayer == true)
        {
            return;
        }

        int dimension = world.provider.dimensionId;

        for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
        {
            if (dimension == i)
            {
                PaintedBiomes.logger.info(String.format("Wrapping the WorldChunkManager (of type %s) of dimension %d with %s ...",
                        world.provider.worldChunkMgr.getClass().toString(), dimension, WorldChunkManagerPaintedBiomes.class.toString()));

                // Re-initialize the ImageHandler after a world loads, to update config values etc.
                ImageHandler imageHandler = ImageHandler.getImageHandler(dimension).init();

                // Don't accidentally re-wrap our own WorldChunkManager...
                if ((world.provider.worldChunkMgr instanceof WorldChunkManagerPaintedBiomes) == false)
                {
                    world.provider.worldChunkMgr = new WorldChunkManagerPaintedBiomes(world, world.provider.worldChunkMgr, imageHandler);
                }

                break;
            }
        }
    }

    private static void overrideChunkProvider(World world)
    {
        int dimension = world.provider.dimensionId;

        Configs conf = Configs.getConfig(dimension);
        if (conf.overrideChunkProvider == true && world instanceof WorldServer)
        {
            PaintedBiomes.logger.info("Attempting to override the ChunkProvider for dimension " + dimension);

            IChunkProvider newChunkProvider = getNewChunkProvider(world, conf.chunkProviderType, conf.chunkProviderOptions);
            if (newChunkProvider == null)
            {
                PaintedBiomes.logger.warn("Invalid/unknown ChunkProvider type '" + conf.chunkProviderType + "'.");
                return;
            }

            /*try
            {
                ReferenceReflection.fieldWorldChunkProvider.setAccessible(true);
                ReferenceReflection.fieldWorldChunkProvider.set(world, newChunkProvider);
            }
            catch (IllegalAccessException e)
            {
                PaintedBiomes.logger.error("Failed to override the used ChunkProvider for World (" + world + ") in dimension " + dimension);
                return;
            }*/

            ((WorldServer)world).theChunkProviderServer.currentChunkProvider = newChunkProvider;
        }
    }

    private static IChunkProvider getNewChunkProvider(World world, String chunkProviderType, String generatorOptions)
    {
        if (chunkProviderType.equals("VANILLA_DEFAULT"))
        {
            return new ChunkProviderGenerate(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled());
        }
        else if (chunkProviderType.equals("VANILLA_FLAT"))
        {
            return new ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        }
        else if (chunkProviderType.equals("VANILLA_HELL"))
        {
            return new ChunkProviderHell(world, world.getSeed());
        }
        else if (chunkProviderType.equals("VANILLA_END"))
        {
            return new ChunkProviderEnd(world, world.getSeed());
        }

        return null;
    }
}
