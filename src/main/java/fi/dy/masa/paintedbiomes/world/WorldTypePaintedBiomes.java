package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldTypePaintedBiomes extends WorldType
{
    public static WorldType instance;

    public WorldTypePaintedBiomes()
    {
        super("PAINTEDBIOMES");
        instance = this;
    }

    @Override
    public WorldChunkManager getChunkManager(World world)
    {
        return new WorldChunkManagerPaintedBiomes(world);
    }

    /*@Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions)
    {
        //return new ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
        return (this == FLAT ? new ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions) : new ChunkProviderGenerate(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled()));
    }*/
}
