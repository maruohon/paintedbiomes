package fi.dy.masa.paintedbiomes.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.world.BiomeProviderPaintedBiomes;
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

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        overrideChunkProvider(event.getWorld());
        overrideBiomeProvider(event.getWorld());
    }

    @SubscribeEvent
    public void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event)
    {
        // The initial world spawn position is created before the WorldEvent.Load fires, so we
        // need this event to cover that case. Otherwise a newly created world (no existing level.dat file yet)
        // will have a mess of regular terrain generation chunks near the spawn chunk...
        overrideChunkProvider(event.getWorld());
        overrideBiomeProvider(event.getWorld());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (Configs.getEffectiveMainConfig().useGenLayer == false)
        {
            ImageHandler.removeImageHandler(event.getWorld().provider.getDimension());
        }
    }

    @SubscribeEvent
    public void onInitBiomeGen(WorldTypeEvent.InitBiomeGens event)
    {
        if (Configs.getEffectiveMainConfig().useGenLayer)
        {
            PaintedBiomes.logger.info("Registering Painted Biomes biome GenLayers");
            ImageHandler.getImageHandler(0).init(event.getSeed());

            event.getNewBiomeGens()[0] = new GenLayerBiomeGeneration(event.getSeed(),
                    event.getOriginalBiomeGens()[0], event.getWorldType(), ChunkGeneratorSettings.Factory.jsonToFactory("").build());
            event.getNewBiomeGens()[1] = new GenLayerBiomeIndex(event.getSeed(),
                    event.getOriginalBiomeGens()[1], event.getWorldType(), ChunkGeneratorSettings.Factory.jsonToFactory("").build());
            event.getNewBiomeGens()[2] = event.getNewBiomeGens()[0];
        }
    }

    private static void overrideBiomeProvider(World world)
    {
        // Not used when using a GenLayer override
        if (world.isRemote == false && Configs.getEffectiveMainConfig().useGenLayer == false)
        {
            int dimension = world.provider.getDimension();

            for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
            {
                if (dimension == i)
                {
                    overrideBiomeProvider(dimension, world);
                    break;
                }
            }
        }
    }

    private static void overrideBiomeProvider(int dimension, World world)
    {
        // Don't accidentally re-wrap our own BiomeProvider...
        if (world.getBiomeProvider() instanceof BiomeProviderPaintedBiomes)
        {
            return;
        }

        PaintedBiomes.logger.info("Wrapping the BiomeProvider (of type {}) of dimension {} with {}",
                world.getBiomeProvider().getClass().getName(), dimension, BiomeProviderPaintedBiomes.class.getName());

        try
        {
            // Re-initialize the ImageHandler when a world loads, to update config values etc.
            ImageHandler imageHandler = ImageHandler.getImageHandler(dimension).init(world.getSeed());

            BiomeProvider newBiomeProvider = new BiomeProviderPaintedBiomes(world, world.getBiomeProvider(), imageHandler);
            ReflectionHelper.setPrivateValue(WorldProvider.class, world.provider, newBiomeProvider, "field_76578_c", "biomeProvider");
        }
        catch (UnableToAccessFieldException e)
        {
            PaintedBiomes.logger.error("Failed to wrap the BiomeProvider of dimension " + dimension);
        }
    }

    private static void overrideChunkProvider(World world)
    {
        if (world.isRemote == false)
        {
            int dimension = world.provider.getDimension();

            for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
            {
                if (dimension == i)
                {
                    overrideChunkProvider(dimension, world);
                    break;
                }
            }
        }
    }

    private static void overrideChunkProvider(int dimension, World world)
    {
        Configs conf = Configs.getConfig(dimension);

        if (conf.overrideChunkProvider && world instanceof WorldServer)
        {
            IChunkGenerator newChunkProvider = getNewChunkProvider(world, conf.chunkProviderType, conf.chunkProviderOptions);

            if (newChunkProvider == null)
            {
                PaintedBiomes.logger.warn("Invalid/unknown ChunkProvider type '{}'", conf.chunkProviderType);
                return;
            }

            PaintedBiomes.logger.info("Attempting to override the ChunkProvider (of type {}) of dimension {} with {}",
                    ((ChunkProviderServer)world.getChunkProvider()).chunkGenerator.getClass().getName(),
                    dimension, newChunkProvider.getClass().getName());

            try
            {
                ReflectionHelper.setPrivateValue(ChunkProviderServer.class, (ChunkProviderServer)world.getChunkProvider(),
                        newChunkProvider, "field_186029_c", "chunkGenerator");
            }
            catch (UnableToAccessFieldException e)
            {
                PaintedBiomes.logger.warn("Failed to override the ChunkProvider for dimension {} with {}",
                        dimension, newChunkProvider.getClass().getName(), e);
            }
        }
    }

    private static IChunkGenerator getNewChunkProvider(World world, String chunkProviderType, String generatorOptions)
    {
        if (chunkProviderType.equals("VANILLA_DEFAULT"))
        {
            return new ChunkGeneratorOverworld(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        }
        else if (chunkProviderType.equals("VANILLA_FLAT"))
        {
            return new ChunkGeneratorFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        }
        else if (chunkProviderType.equals("VANILLA_HELL"))
        {
            return new ChunkGeneratorHell(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed());
        }
        else if (chunkProviderType.equals("VANILLA_END"))
        {
            return new ChunkGeneratorEnd(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed(), new BlockPos(100, 50, 0));
        }

        return null;
    }
}
