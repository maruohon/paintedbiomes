package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageRegion implements IImageReader
{
    protected final int dimension;
    protected final String name;
    protected static final Random rand = new Random();
    protected final int templateRotation;

    protected final int unpaintedAreaBiomeID;
    protected final int templateUndefinedAreaBiomeID;
    protected final boolean useTemplateRotation;

    protected BufferedImage imageData;
    protected int areaSizeX;
    protected int areaSizeZ;

    public ImageRegion(int dimension, int regionX, int regionZ, long seed, File path)
    {
        this.dimension = dimension;
        this.name = "r." + regionX + "." + regionZ;

        this.readImageTemplate(new File(path, this.name + ".png"));
        Configs conf = Configs.getConfig(this.dimension);
        this.unpaintedAreaBiomeID = conf.unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = conf.templateUndefinedAreaBiome;
        this.useTemplateRotation = conf.useTemplateRandomRotation;

        // non-square template image with random template rotations enabled...
        if (this.useTemplateRotation == true && this.areaSizeX != this.areaSizeZ)
        {
            String str = String.format("*** WARNING: Template random rotations enabled, but the template image for " +
                                       "region r.%d.%d is not square! Clipping the template to a square!", regionX, regionZ);
            PaintedBiomes.logger.warn(str);

            // Clip the template image to a square
            this.areaSizeX = Math.min(this.areaSizeX, this.areaSizeZ);
            this.areaSizeZ = Math.min(this.areaSizeX, this.areaSizeZ);
        }

        this.templateRotation = this.getTemplateRotation(seed, regionX, regionZ);
    }

    public void readImageTemplate(File imageFile)
    {
        this.imageData = null;

        try
        {
            if (imageFile.exists() == true)
            {
                this.imageData = ImageIO.read(imageFile);

                if (this.imageData != null)
                {
                    this.areaSizeX = this.imageData.getWidth();
                    this.areaSizeZ = this.imageData.getHeight();
                    PaintedBiomes.logger.info("Successfully read image template for region '" + this.name + "' from '" + imageFile.getAbsolutePath() + "'");

                    return;
                }
            }

            //PaintedBiomes.logger.warn("Failed to read image template for region '" + this.name + "' from '" + imageFile.getAbsolutePath() + "'");
        }
        catch (IOException e)
        {
            //PaintedBiomes.logger.warn("Failed to read image template for region '" + this.name + "' from '" + imageFile.getAbsolutePath() + "'");
        }
    }

    protected int getTemplateRotation(long seed, long posX, long posZ)
    {
        if (this.useTemplateRotation == false)
        {
            return 0;
        }

        rand.setSeed(seed);
        long l1 = rand.nextLong() / 2L * 2L + 1L;
        long l2 = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed(posX * l1 + posZ * l2 ^ seed);

        return rand.nextInt(4);
    }

    protected int getImageX(int areaX, int areaZ)
    {
        // normal (0 degrees) template rotation
        if (this.templateRotation == 0)
        {
            return areaX;
        }

        // 90 degree template rotation clock-wise
        if (this.templateRotation == 1)
        {
            return areaZ;
        }

        // 180 degree template rotation clock-wise
        if (this.templateRotation == 2)
        {
            return this.areaSizeX - areaX - 1;
        }

        // 270 degree template rotation clock-wise
        return this.areaSizeZ - areaZ - 1;
    }

    protected int getImageY(int areaX, int areaZ)
    {
        // normal (0 degrees) template rotation
        if (this.templateRotation == 0)
        {
            return areaZ;
        }

        // 90 degree template rotation clock-wise
        if (this.templateRotation == 1)
        {
            return this.areaSizeX - areaX - 1;
        }

        // 180 degree template rotation clock-wise
        if (this.templateRotation == 2)
        {
            return this.areaSizeZ - areaZ - 1;
        }

        // 270 degree template rotation clock-wise
        return areaX;
    }

    protected int getUnpaintedAreaBiomeID(int defaultBiomeID)
    {
        // If there is a biome defined for unpainted areas, then use that, otherwise use the biome from the regular terrain generation
        return this.unpaintedAreaBiomeID != -1 ? this.unpaintedAreaBiomeID : defaultBiomeID;
    }

    protected int getUndefinedAreaBiomeID(int defaultBiomeID)
    {
        // Return the Biome ID for the undefined areas, if one has been set, otherwise return the one from the regular terrain generation
        return this.templateUndefinedAreaBiomeID != -1 ? this.templateUndefinedAreaBiomeID : defaultBiomeID;
    }

    protected int getAreaX(int blockX)
    {
        return blockX & 0x1FF;
        //return (blockX % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
    }

    protected int getAreaZ(int blockZ)
    {
        return blockZ & 0x1FF;
        //return (blockZ % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
    }

    public boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        int areaX = this.getAreaX(blockX);
        int areaZ = this.getAreaZ(blockZ);

        return this.imageData != null && areaX < this.areaSizeX && areaZ < this.areaSizeZ;
    }

    @Override
    public boolean isBiomeDefinedAt(int blockX, int blockZ)
    {
        if (this.isLocationCoveredByTemplate(blockX, blockZ) == false)
        {
            return this.unpaintedAreaBiomeID != -1;
        }

        int areaX = this.getAreaX(blockX);
        int areaZ = this.getAreaZ(blockZ);
        int imageX = this.getImageX(areaX, areaZ);
        int imageY = this.getImageY(areaX, areaZ);
        int[] alpha = new int[1];

        try
        {
            WritableRaster raster = this.imageData.getAlphaRaster();
            if (raster != null)
            {
                raster.getPixel(imageX, imageY, alpha);
            }
            else
            {
                alpha[0] = 0xFF;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            PaintedBiomes.logger.fatal("Error reading the alpha channel of the template image");
        }

        // Completely transparent pixel
        if (alpha[0] == 0x00)
        {
            return this.templateUndefinedAreaBiomeID != -1;
        }

        return ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(imageX, imageY)) != -1;
    }

    protected int getBiomeIdFromTemplateImage(int blockX, int blockZ, int defaultBiomeID)
    {
        int areaX = this.getAreaX(blockX);
        int areaZ = this.getAreaZ(blockZ);
        int imageX = this.getImageX(areaX, areaZ);
        int imageY = this.getImageY(areaX, areaZ);
        int[] alpha = new int[1];

        try
        {
            WritableRaster raster = this.imageData.getAlphaRaster();
            if (raster != null)
            {
                raster.getPixel(imageX, imageY, alpha);
            }
            else
            {
                alpha[0] = 0xFF;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            PaintedBiomes.logger.fatal("Error reading the alpha channel of the template image");
        }

        // Completely transparent pixel
        if (alpha[0] == 0x00)
        {
            return this.getUndefinedAreaBiomeID(defaultBiomeID);
        }

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(imageX, imageY));

        return biomeID != -1 ? biomeID : this.getUndefinedAreaBiomeID(defaultBiomeID);
    }

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.isLocationCoveredByTemplate(blockX, blockZ) == false)
        {
            return this.getUnpaintedAreaBiomeID(defaultBiomeID);
        }

        return this.getBiomeIdFromTemplateImage(blockX, blockZ, defaultBiomeID);
    }
}
