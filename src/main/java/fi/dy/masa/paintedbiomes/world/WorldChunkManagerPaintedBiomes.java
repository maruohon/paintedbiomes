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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.paintedbiomes.image.ImageHandler;


public class WorldChunkManagerPaintedBiomes extends WorldChunkManager
{
    protected BiomeCache biomeCache;
    protected WorldChunkManager worldChunkManagerParent;
    protected ImageHandler imageHandler;

    public WorldChunkManagerPaintedBiomes(World world, WorldChunkManager worldChunkMgrParent, ImageHandler imageHandler)
    {
        super(world);
        this.worldChunkManagerParent = worldChunkMgrParent;
        this.imageHandler = imageHandler;
        this.biomeCache = new BiomeCache(this);
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
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int scaledX, int scaledZ, int width, int height)
    {
        int size = width * height;
        if (biomes == null || biomes.length < size)
        {
            biomes = new BiomeGenBase[size];
        }

        int endX = scaledX + width;
        int endZ = scaledZ + height;

        // Get the biomes from the regular WorldChunkManager for this dimension
        biomes = this.worldChunkManagerParent.getBiomesForGeneration(biomes, scaledX, scaledZ, width, height);

        try
        {
            for (int i = 0, tmpZ = scaledZ; tmpZ < endZ; tmpZ++)
            {
                for (int tmpX = scaledX; tmpX < endX; tmpX++, i++)
                {
                    biomes[i] = this.getBiomeFromTemplateAt(tmpX << 2, tmpZ << 2, biomes[i].biomeID);
                }
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("BiomeBlock");
            crashreportcategory.addCrashSection("biomes[] size", Integer.valueOf(biomes.length));
            crashreportcategory.addCrashSection("x", Integer.valueOf(scaledX));
            crashreportcategory.addCrashSection("z", Integer.valueOf(scaledZ));
            crashreportcategory.addCrashSection("w", Integer.valueOf(width));
            crashreportcategory.addCrashSection("h", Integer.valueOf(height));
            throw new ReportedException(crashreport);
        }

        return biomes;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomes, int x, int z, int width, int height)
    {
        return this.getBiomeGenAt(biomes, x, z, width, height, true);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int height, boolean cacheFlag)
    {
        int size = width * height;
        if (biomes == null || biomes.length < size)
        {
            biomes = new BiomeGenBase[size];
        }

        // Get cached biomes
        if (cacheFlag == true && width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0)
        {
            BiomeGenBase[] bgb = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(bgb, 0, biomes, 0, size);
            return biomes;
        }
        else
        {
            // Get the biomes from the regular WorldChunkManager for this dimension
            biomes = this.worldChunkManagerParent.getBiomeGenAt(biomes, x, z, width, height, cacheFlag);

            int endX = x + width;
            int endZ = z + height;

            try
            {
                for (int i = 0, tmpZ = z; tmpZ < endZ; tmpZ++)
                {
                    for (int tmpX = x; tmpX < endX; tmpX++, i++)
                    {
                        biomes[i] = this.getBiomeFromTemplateAt(tmpX, tmpZ, biomes[i].biomeID);
                    }
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("BiomeBlock");
                crashreportcategory.addCrashSection("biomes[] size", Integer.valueOf(biomes.length));
                crashreportcategory.addCrashSection("x", Integer.valueOf(x));
                crashreportcategory.addCrashSection("z", Integer.valueOf(z));
                crashreportcategory.addCrashSection("w", Integer.valueOf(width));
                crashreportcategory.addCrashSection("h", Integer.valueOf(height));
                throw new ReportedException(crashreport);
            }
        }

        return biomes;
    }

    protected BiomeGenBase getBiomeFromTemplateAt(int blockX, int blockZ, int defaultBiomeID)
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
        int size = width * height;
        if (rainfall == null || rainfall.length < size)
        {
            rainfall = new float[size];
        }

        rainfall = this.worldChunkManagerParent.getRainfall(rainfall, x, z, width, height);
        BiomeGenBase biomes[] = this.getBiomeGenAt(new BiomeGenBase[size], x, z, width, height, false);

        int endX = x + width;
        int endZ = z + height;

        try
        {
            for (int i = 0, tmpZ = z; tmpZ < endZ; tmpZ++)
            {
                for (int tmpX = x; tmpX < endX; tmpX++, i++)
                {
                    if (this.imageHandler.isBiomeDefinedAt(tmpX, tmpZ) == true)
                    {
                        float f = (float)biomes[i].getIntRainfall() / 65536.0f;
                        if (f > 1.0f)
                        {
                            f = 1.0f;
                        }

                        rainfall[i] = f;
                    }
                }
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("DownfallBlock");
            crashreportcategory.addCrashSection("downfalls[] size", Integer.valueOf(rainfall.length));
            crashreportcategory.addCrashSection("x", Integer.valueOf(x));
            crashreportcategory.addCrashSection("z", Integer.valueOf(z));
            crashreportcategory.addCrashSection("w", Integer.valueOf(width));
            crashreportcategory.addCrashSection("h", Integer.valueOf(height));
            throw new ReportedException(crashreport);
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
        int startX = x - r >> 2;
        int startZ = z - r >> 2;
        int endX = x + r >> 2;
        int endZ = z + r >> 2;
        int width = endX - startX + 1;
        int height = endZ - startZ + 1;

        BiomeGenBase[] biomes = this.getBiomesForGeneration(new BiomeGenBase[width * height], startX, startZ, width, height);

        for (int i = 0; i < width * height; i++)
        {
            if (list.contains(biomes[i]) == false)
            {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ChunkPosition findBiomePosition(int x, int z, int r, List list, Random rand)
    {
        int startX = x - r >> 2;
        int startZ = z - r >> 2;
        int endX = x + r >> 2;
        int endZ = z + r >> 2;
        int width = endX - startX + 1;
        int height = endZ - startZ + 1;
        int size = width * height;
        ChunkPosition chunkPosition = null;

        BiomeGenBase[] biomes = this.getBiomesForGeneration(new BiomeGenBase[size], startX, startZ, width, height);

        for (int i = 0, matches = 0; i < size; ++i)
        {
            int chunkX = startX + i % width << 2;
            int chunkZ = startZ + i / width << 2;

            if (list.contains(biomes[i]) && (chunkPosition == null || rand.nextInt(matches + 1) == 0))
            {
                chunkPosition = new ChunkPosition(chunkX, 0, chunkZ);
                matches++;
            }
        }

        return chunkPosition;
    }
}
