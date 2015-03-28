package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import fi.dy.masa.paintedbiomes.image.ImageHandler;

public class WorldChunkManagerPaintedBiomes extends WorldChunkManager
{
    private World world;
    private GenLayer genBiomes;
    /** A GenLayer containing the indices into BiomeGenBase.biomeList[] */
    private GenLayer biomeIndexLayer;
    /** The BiomeCache object for this world. */
    private BiomeCache biomeCache;

    public WorldChunkManagerPaintedBiomes(World world)
    {
        super(world);
        this.world = world;
        this.biomeCache = new BiomeCache(this);
        GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(world.getSeed(), world.getWorldInfo().getTerrainType());
        agenlayer = getModdedBiomeGenerators(world.getWorldInfo().getTerrainType(), world.getSeed(), agenlayer);
        this.genBiomes = agenlayer[0];
        this.biomeIndexLayer = agenlayer[1];
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z)
    {
        return this.biomeCache.getBiomeGenAt(x, z);
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int quadrupleChunkX, int quadrupleChunkZ, int width, int length)
    {
        IntCache.resetIntCache();

        int len = width * length;
        if (biomes == null || biomes.length < len)
        {
            biomes = new BiomeGenBase[len];
        }

        int endX = quadrupleChunkX + width;
        int endZ = quadrupleChunkZ + length;
        int[] aint = this.genBiomes.getInts(quadrupleChunkX, quadrupleChunkZ, width, length);

        for (int i = 0, qcz = quadrupleChunkZ; qcz < endZ; ++qcz)
        {
            for (int qcx = quadrupleChunkX; qcx < endX; ++qcx)
            {
                biomes[i] = this.getBiomeAt(qcx << 2, qcz << 2, aint[i]);
                i++;
            }
        }

        return biomes;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomes, int x, int z, int width, int length)
    {
        return this.getBiomeGenAt(biomes, x, z, width, length, true);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int length, boolean cacheFlag)
    {
        IntCache.resetIntCache();

        int len = width * length;
        if (biomes == null || biomes.length < len)
        {
            biomes = new BiomeGenBase[len];
        }

        // Get cached biomes
        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0)
        {
            BiomeGenBase[] bgb = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(bgb, 0, biomes, 0, len);
            return biomes;
        }
        else
        {
            int endX = x + width;
            int endZ = z + length;
            int[] aint = this.biomeIndexLayer.getInts(x, z, width, length);

            for (int i = 0, bz = z; bz < endZ; ++bz)
            {
                for (int bx = x; bx < endX; ++bx)
                {
                    biomes[i] = this.getBiomeAt(bx, bz, aint[i]);
                    i++;
                }
            }
        }

        return biomes;
    }

    private BiomeGenBase getBiomeAt(int blockX, int blockZ, int defaultBiomeID)
    {
        return ImageHandler.getImageHandler(this.world.provider.dimensionId).getBiomeAt(blockX, blockZ, defaultBiomeID);
    }

    /*@Override
    public float[] getRainfall(float[] rainfall, int x, int z, int width, int length)
    {
        int len = width * length;
        if (rainfall == null || rainfall.length < len)
        {
            rainfall = new float[len];
        }

        // TODO

        return rainfall;
    }*/

    // TODO
    /*@Override
    public boolean areBiomesViable(int x, int z, int r, List list)
    {
    }

    // TODO
    @Override
    public ChunkPosition findBiomePosition(int x, int z, int r, List list, Random rand)
    {
    }*/

    @Override
    public void cleanupCache()
    {
        this.biomeCache.cleanupCache();
    }
}
