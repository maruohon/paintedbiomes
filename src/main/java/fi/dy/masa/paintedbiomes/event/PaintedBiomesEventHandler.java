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
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
        World world = event.getWorld();

        if (world.isRemote == false)
        {
            this.onWorldLoad(world);
            this.overrideChunkGenerator(world);
            this.overrideBiomeProvider(world);
        }
    }

    @SubscribeEvent
    public void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event)
    {
        World world = event.getWorld();

        // The initial world spawn position is created before the WorldEvent.Load fires, so we
        // need this event to cover that case. Otherwise a newly created world (no existing level.dat file yet)
        // will have a mess of regular terrain generation chunks near the spawn chunk...
        if (world.isRemote == false)
        {
            this.overrideChunkGenerator(world);
            this.overrideBiomeProvider(world);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (event.getWorld().isRemote == false && Configs.getEffectiveMainConfig().useGenLayer == false)
        {
            ImageHandler.removeImageHandler(event.getWorld().provider.getDimension());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public void onInitBiomeGen(WorldTypeEvent.InitBiomeGens event)
    {
        if (Configs.getEffectiveMainConfig().useGenLayer)
        {
            PaintedBiomes.logger.info("Registering Painted Biomes biome GenLayers");
            ImageHandler.getImageHandler(0).init(event.getSeed());
            GenLayer[] oldGens = event.getNewBiomeGens();
            GenLayer[] newGens = new GenLayer[oldGens.length];
            newGens[0] = new GenLayerBiomeGeneration(event.getSeed(), oldGens[0], event.getWorldType(), ChunkGeneratorSettings.Factory.jsonToFactory("").build());
            newGens[1] = new GenLayerBiomeIndex(event.getSeed(), oldGens[1], event.getWorldType(), ChunkGeneratorSettings.Factory.jsonToFactory("").build());
            newGens[2] = newGens[0];
            event.setNewBiomeGens(newGens);
        }
    }

    private void onWorldLoad(World world)
    {
        ImageHandler handler = ImageHandler.getImageHandlerIfExists(world.provider.getDimension());

        if (handler != null && world instanceof WorldServer)
        {
            handler.onWorldLoad((WorldServer) world);
        }
    }

    private void overrideBiomeProvider(World world)
    {
        // Not used when using a GenLayer override
        if (Configs.getEffectiveMainConfig().useGenLayer == false)
        {
            int dimension = world.provider.getDimension();

            for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
            {
                if (dimension == i)
                {
                    this.overrideBiomeProvider(dimension, world);
                    break;
                }
            }
        }
    }

    private void overrideBiomeProvider(int dimension, World world)
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

    private void overrideChunkGenerator(World world)
    {
        int dimension = world.provider.getDimension();

        for (int i : Configs.getEffectiveMainConfig().enabledInDimensions)
        {
            if (dimension == i)
            {
                this.overrideChunkGenerator(dimension, world);
                break;
            }
        }
    }

    private void overrideChunkGenerator(int dimension, World world)
    {
        Configs conf = Configs.getConfig(dimension);

        if (conf.overrideChunkProvider && world instanceof WorldServer)
        {
            IChunkGenerator newChunkProvider = this.getNewChunkGenerator(world, conf.chunkProviderType, conf.chunkProviderOptions);

            if (newChunkProvider == null)
            {
                PaintedBiomes.logger.warn("Invalid/unknown ChunkGenerator type '{}'", conf.chunkProviderType);
                return;
            }

            PaintedBiomes.logger.info("Attempting to override the ChunkGenerator (of type {}) of dimension {} with {}",
                    ((ChunkProviderServer) world.getChunkProvider()).chunkGenerator.getClass().getName(),
                    dimension, newChunkProvider.getClass().getName());

            try
            {
                ReflectionHelper.setPrivateValue(ChunkProviderServer.class, (ChunkProviderServer)world.getChunkProvider(),
                        newChunkProvider, "field_186029_c", "chunkGenerator");
            }
            catch (UnableToAccessFieldException e)
            {
                PaintedBiomes.logger.warn("Failed to override the ChunkGenerator for dimension {} with {}",
                        dimension, newChunkProvider.getClass().getName(), e);
            }
        }
    }

    private IChunkGenerator getNewChunkGenerator(World world, String chunkGeneratorType, String generatorOptions)
    {
        if (chunkGeneratorType.equals("VANILLA_DEFAULT"))
        {
            return new ChunkGeneratorOverworld(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        }
        else if (chunkGeneratorType.equals("VANILLA_FLAT"))
        {
            return new ChunkGeneratorFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        }
        else if (chunkGeneratorType.equals("VANILLA_HELL"))
        {
            return new ChunkGeneratorHell(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed());
        }
        else if (chunkGeneratorType.equals("VANILLA_END"))
        {
            return new ChunkGeneratorEnd(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed(), new BlockPos(100, 50, 0));
        }

        return null;
    }
}
