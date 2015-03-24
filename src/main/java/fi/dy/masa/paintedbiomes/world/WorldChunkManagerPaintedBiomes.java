package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldChunkManagerPaintedBiomes extends WorldChunkManager
{
    public WorldChunkManagerPaintedBiomes(World world)
    {
        super(world);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int length, boolean cacheFlag)
    {
        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0)
        {
            return super.getBiomeGenAt(biomes, x, z, width, length, cacheFlag);
        }
        else
        {
            int len = width * length;
            if (biomes == null || biomes.length < len)
            {
                biomes = new BiomeGenBase[len];
            }

            for (int i = 0; i < len; ++i)
            {
                if (Math.abs(x) < 32 && Math.abs(z) < 32)
                {
                    biomes[i] = BiomeGenBase.megaTaiga;
                }
                else
                {
                    biomes[i] = BiomeGenBase.birchForestHills;
                }
            }
        }

        return biomes;
    }
}
