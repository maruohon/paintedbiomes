package fi.dy.masa.paintedbiomes.event;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.ChunkProviderServer;

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
    public void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event)
    {
        // The initial world spawn position is created before the WorldEvent.Load fires, so we
        // need this event to cover that case. Otherwise a newly created world (no existing level.dat file yet)
        // will have a mess of regular terrain generation chunks near the spawn chunk...
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

        PaintedBiomes.logger.info("Registering Painted Biomes biome GenLayers");
        ImageHandler.getImageHandler(0).init(event.seed);
        event.newBiomeGens[0] = new GenLayerBiomeGeneration(event.seed, event.originalBiomeGens[0], event.worldType);
        event.newBiomeGens[1] = new GenLayerBiomeIndex(event.seed, event.originalBiomeGens[1], event.worldType);
        event.newBiomeGens[2] = event.newBiomeGens[0];
    }

    private static void overrideWorldChunkManager(World world)
    {
        // Not used when using a GenLayer override, and don't accidentally re-wrap our own WorldChunkManager...
        if (world.isRemote == true || Configs.getEffectiveMainConfig().useGenLayer == true ||
            world.provider.worldChunkMgr instanceof WorldChunkManagerPaintedBiomes)
        {
            return;
        }

        int dimension = world.provider.dimensionId;

        for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
        {
            if (dimension == i)
            {
                overrideWorldChunkManager(dimension, world);
                break;
            }
        }
    }

    private static void overrideWorldChunkManager(int dimension, World world)
    {
        if (world.getWorldChunkManager() instanceof WorldChunkManagerPaintedBiomes)
        {
            return;
        }

        PaintedBiomes.logger.info(String.format("Wrapping the WorldChunkManager (of type %s) of dimension %d with %s",
                world.getWorldChunkManager().getClass().getName(), dimension, WorldChunkManagerPaintedBiomes.class.getName()));

        // Re-initialize the ImageHandler when a world loads, to update config values etc.
        ImageHandler imageHandler = ImageHandler.getImageHandler(dimension).init(world.getSeed());

        world.provider.worldChunkMgr = new WorldChunkManagerPaintedBiomes(world, world.getWorldChunkManager(), imageHandler);
    }

    private static void overrideChunkProvider(World world)
    {
        if (world.isRemote == true)
        {
            return;
        }

        int dimension = world.provider.dimensionId;

        for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
        {
            if (dimension == i)
            {
                overrideChunkProvider(dimension, world);
                break;
            }
        }
    }

    private static void overrideChunkProvider(int dimension, World world)
    {
        Configs conf = Configs.getConfig(dimension);
        if (conf.overrideChunkProvider == true && world instanceof WorldServer)
        {
            IChunkProvider newChunkProvider = getNewChunkProvider(world, conf.chunkProviderType, conf.chunkProviderOptions);
            if (newChunkProvider == null)
            {
                PaintedBiomes.logger.warn("Invalid/unknown ChunkProvider type '" + conf.chunkProviderType + "'.");
                return;
            }

            PaintedBiomes.logger.info(String.format("Attempting to override the ChunkProvider (of type %s) of dimension %d with %s",
                    ((WorldServer)world).theChunkProviderServer.currentChunkProvider.getClass().getName(), dimension, newChunkProvider.getClass().getName()));

            try
            {
                ((WorldServer)world).theChunkProviderServer.currentChunkProvider = newChunkProvider;
                ((ChunkProviderServer)world.getChunkProvider()).currentChunkProvider = newChunkProvider;
            }
            catch (Exception e)
            {
                PaintedBiomes.logger.warn("Failed to override the ChunkProvider for dimension " + dimension + " with " + newChunkProvider.getClass().getName());
                PaintedBiomes.logger.warn("" + e.getMessage());
                //e.printStackTrace();
            }
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
