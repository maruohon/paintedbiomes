package fi.dy.masa.paintedbiomes.world;

import java.util.List;
import java.util.Random;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.paintedbiomes.image.ImageHandler;


public class WorldChunkManagerPaintedBiomes extends WorldChunkManager
{
    protected World world;
    protected GenLayer genBiomes;
    protected GenLayer biomeIndexLayer;
    protected BiomeCache biomeCache;
    protected WorldChunkManager worldChunkManagerParent;
    protected ImageHandler imageHandler;

    public WorldChunkManagerPaintedBiomes(World world, WorldChunkManager worldChunkMgrParent, ImageHandler imageHandler)
    {
        super(world);
        this.world = world;
        this.worldChunkManagerParent = worldChunkMgrParent;
        this.imageHandler = imageHandler;
        this.biomeCache = new BiomeCache(this);
        GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(world.getSeed(), world.getWorldInfo().getTerrainType());
        agenlayer = getModdedBiomeGenerators(world.getWorldInfo().getTerrainType(), world.getSeed(), agenlayer);
        this.genBiomes = agenlayer[0];
        this.biomeIndexLayer = agenlayer[1];
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getBiomesToSpawnIn()
    {
        return this.worldChunkManagerParent.getBiomesToSpawnIn();
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
        return BiomeGenBase.getBiome(this.imageHandler.getBiomeIDAt(blockX, blockZ, defaultBiomeID));
    }

    @Override
    public void cleanupCache()
    {
        this.biomeCache.cleanupCache();
    }

    @Override
    public float[] getRainfall(float[] rainfall, int x, int z, int width, int height)
    {
        // TODO

        int len = width * height;
        if (rainfall == null || rainfall.length < len)
        {
            rainfall = new float[len];
        }

        int[] aint = this.biomeIndexLayer.getInts(x, z, width, height);

        for (int i = 0; i < width * height; ++i)
        {
            try
            {
                float f = 0.5f;
                f = (float)BiomeGenBase.getBiome(aint[i]).getIntRainfall() / 65536.0f;

                if (f > 1.0F)
                {
                    f = 1.0F;
                }

                rainfall[i] = f;
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("DownfallBlock");
                crashreportcategory.addCrashSection("biome id", Integer.valueOf(i));
                crashreportcategory.addCrashSection("downfalls[] size", Integer.valueOf(rainfall.length));
                crashreportcategory.addCrashSection("x", Integer.valueOf(x));
                crashreportcategory.addCrashSection("z", Integer.valueOf(z));
                crashreportcategory.addCrashSection("w", Integer.valueOf(width));
                crashreportcategory.addCrashSection("h", Integer.valueOf(height));
                throw new ReportedException(crashreport);
            }
        }

        return rainfall;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getTemperatureAtHeight(float p_76939_1_, int p_76939_2_)
    {
        return this.worldChunkManagerParent.getTemperatureAtHeight(p_76939_1_, p_76939_2_);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean areBiomesViable(int x, int z, int r, List list)
    {
        // TODO
        return this.worldChunkManagerParent.areBiomesViable(x, z, r, list);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ChunkPosition findBiomePosition(int x, int z, int r, List list, Random rand)
    {
        // TODO
        return this.worldChunkManagerParent.findBiomePosition(x, z, r, list, rand);
    }
}
