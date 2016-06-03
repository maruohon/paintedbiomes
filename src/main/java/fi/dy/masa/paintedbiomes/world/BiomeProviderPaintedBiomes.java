package fi.dy.masa.paintedbiomes.world;

import java.util.List;
import java.util.Random;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.paintedbiomes.image.ImageHandler;


public class BiomeProviderPaintedBiomes extends BiomeProvider
{
    protected BiomeCache biomeCache;
    protected BiomeProvider biomeProviderParent;
    protected ImageHandler imageHandler;

    public BiomeProviderPaintedBiomes(World world, BiomeProvider biomeProviderParent, ImageHandler imageHandler)
    {
        super(world.getWorldInfo());
        this.biomeProviderParent = biomeProviderParent;
        this.imageHandler = imageHandler;
        this.biomeCache = new BiomeCache(this);
    }

    @Override
    public List<Biome> getBiomesToSpawnIn()
    {
        return this.biomeProviderParent.getBiomesToSpawnIn();
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return this.getBiome(pos, null);
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome biomeGenBaseIn)
    {
        return this.biomeCache.getBiome(pos.getX(), pos.getZ(), biomeGenBaseIn);
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int scaledX, int scaledZ, int width, int height)
    {
        int size = width * height;
        if (biomes == null || biomes.length < size)
        {
            biomes = new Biome[size];
        }

        int endX = scaledX + width;
        int endZ = scaledZ + height;

        // Get the biomes from the regular WorldChunkManager for this dimension
        biomes = this.biomeProviderParent.getBiomesForGeneration(biomes, scaledX, scaledZ, width, height);

        try
        {
            for (int i = 0, tmpZ = scaledZ; tmpZ < endZ; tmpZ++)
            {
                for (int tmpX = scaledX; tmpX < endX; tmpX++, i++)
                {
                    biomes[i] = this.getBiomeFromTemplateAt(tmpX << 2, tmpZ << 2, Biome.getIdForBiome(biomes[i]));
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
    public Biome[] getBiomes(Biome[] oldBiomeList, int x, int z, int width, int depth)
    {
        return this.getBiomes(oldBiomeList, x, z, width, depth, true);
    }

    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int length, boolean cacheFlag)
    {
        int size = width * length;
        if (biomes == null || biomes.length < size)
        {
            biomes = new Biome[size];
        }

        // Get cached biomes
        if (cacheFlag == true && width == 16 && length == 16 && (x & 0xF) == 0 && (z & 0xF) == 0)
        {
            Biome[] bgb = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(bgb, 0, biomes, 0, size);
            return biomes;
        }
        else
        {
            // Get the biomes from the regular WorldChunkManager for this dimension
            biomes = this.biomeProviderParent.getBiomes(biomes, x, z, width, length, cacheFlag);

            int endX = x + width;
            int endZ = z + length;

            try
            {
                for (int i = 0, tmpZ = z; tmpZ < endZ; tmpZ++)
                {
                    for (int tmpX = x; tmpX < endX; tmpX++, i++)
                    {
                        biomes[i] = this.getBiomeFromTemplateAt(tmpX, tmpZ, Biome.getIdForBiome(biomes[i]));
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

    protected Biome getBiomeFromTemplateAt(int blockX, int blockZ, int defaultBiomeID)
    {
        return Biome.getBiome(this.imageHandler.getBiomeIDAt(blockX, blockZ, defaultBiomeID));
    }

    @Override
    public void cleanupCache()
    {
        this.biomeCache.cleanupCache();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getTemperatureAtHeight(float p_76939_1_, int p_76939_2_)
    {
        return this.biomeProviderParent.getTemperatureAtHeight(p_76939_1_, p_76939_2_);
    }

    @Override
    public boolean areBiomesViable(int x, int z, int r, List<Biome> list)
    {
        int startX = x - r >> 2;
        int startZ = z - r >> 2;
        int endX = x + r >> 2;
        int endZ = z + r >> 2;
        int width = endX - startX + 1;
        int height = endZ - startZ + 1;
        int size = width * height;

        Biome[] biomes = this.getBiomesForGeneration(new Biome[size], startX, startZ, width, height);

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
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random)
    {
        int startX = x - range >> 2;
        int startZ = z - range >> 2;
        int endX = x + range >> 2;
        int endZ = z + range >> 2;
        int width = endX - startX + 1;
        int height = endZ - startZ + 1;
        int size = width * height;
        BlockPos blockPos = null;

        Biome[] bgb = this.getBiomesForGeneration(new Biome[size], startX, startZ, width, height);

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
