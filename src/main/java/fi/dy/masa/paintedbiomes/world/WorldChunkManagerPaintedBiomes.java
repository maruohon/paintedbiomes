package fi.dy.masa.paintedbiomes.world;

import java.util.List;
import java.util.Random;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

import fi.dy.masa.paintedbiomes.image.ImageHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


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

    @Override
    public List<BiomeGenBase> getBiomesToSpawnIn()
    {
        return this.worldChunkManagerParent.getBiomesToSpawnIn();
    }

    @Override
    public BiomeGenBase getBiomeGenerator(BlockPos pos)
    {
        return this.getBiomeGenerator(pos, (BiomeGenBase)null);
    }

    @Override
    public BiomeGenBase getBiomeGenerator(BlockPos pos, BiomeGenBase biomeGenBaseIn)
    {
        return this.biomeCache.func_180284_a(pos.getX(), pos.getZ(), biomeGenBaseIn);
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
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] oldBiomeList, int x, int z, int width, int depth)
    {
        return this.getBiomeGenAt(oldBiomeList, x, z, width, depth, true);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int length, boolean cacheFlag)
    {
        int size = width * length;
        if (biomes == null || biomes.length < size)
        {
            biomes = new BiomeGenBase[size];
        }

        // Get cached biomes
        if (cacheFlag == true && width == 16 && length == 16 && (x & 0xF) == 0 && (z & 0xF) == 0)
        {
            BiomeGenBase[] bgb = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(bgb, 0, biomes, 0, size);
            return biomes;
        }
        else
        {
            // Get the biomes from the regular WorldChunkManager for this dimension
            biomes = this.worldChunkManagerParent.getBiomeGenAt(biomes, x, z, width, length, cacheFlag);

            int endX = x + width;
            int endZ = z + length;

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
                crashreportcategory.addCrashSection("h", Integer.valueOf(length));
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

    @Override
    public boolean areBiomesViable(int x, int z, int r, List<BiomeGenBase> list)
    {
        int startX = x - r >> 2;
        int startZ = z - r >> 2;
        int endX = x + r >> 2;
        int endZ = z + r >> 2;
        int width = endX - startX + 1;
        int height = endZ - startZ + 1;
        int size = width * height;

        BiomeGenBase[] biomes = this.getBiomesForGeneration(new BiomeGenBase[size], startX, startZ, width, height);

        for (int i = 0; i < size; i++)
        {
            if (list.contains(biomes[i]) == false)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<BiomeGenBase> biomes, Random random)
    {
        int startX = x - range >> 2;
        int startZ = z - range >> 2;
        int endX = x + range >> 2;
        int endZ = z + range >> 2;
        int width = endX - startX + 1;
        int height = endZ - startZ + 1;
        int size = width * height;
        BlockPos blockPos = null;

        BiomeGenBase[] bgb = this.getBiomesForGeneration(new BiomeGenBase[size], startX, startZ, width, height);

        for (int i = 0, matches = 0; i < size; i++)
        {
            int posX = startX + i % width << 2;
            int posZ = startZ + i / width << 2;

            if (biomes.contains(bgb[i]) && (blockPos == null || random.nextInt(matches + 1) == 0))
            {
                blockPos = new BlockPos(posX, 0, posZ);
                matches++;
            }
        }

        return blockPos;
    }
}
