package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public abstract class ImageBase implements IImageReader
{
    protected static final Random rand = new Random();
    protected final long randLong1;
    protected final long randLong2;

    protected BufferedImage imageData;
    protected final int dimension;
    protected final long worldSeed;

    protected final boolean useTemplateRotation;
    protected final int unpaintedAreaBiomeID;
    protected final int templateUndefinedAreaBiomeID;

    protected int areaSizeX;
    protected int areaSizeZ;

    protected int templateRotation;

    public ImageBase(int dimension, long seed)
    {
        this.dimension = dimension;
        this.worldSeed = seed;

        Configs conf = Configs.getConfig(this.dimension);
        this.useTemplateRotation = conf.useTemplateRandomRotation;
        this.unpaintedAreaBiomeID = conf.unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = conf.templateUndefinedAreaBiome;

        rand.setSeed(this.worldSeed);
        this.randLong1 = rand.nextLong() / 2L * 2L + 1L;
        this.randLong2 = rand.nextLong() / 2L * 2L + 1L;
    }

    protected void setTemplateDimensions()
    {
        if (this.imageData == null)
        {
            PaintedBiomes.logger.warn("null template image while trying to get template dimensions");
            return;
        }

        // 0 degree or 180 degree template rotation
        if ((this.templateRotation & 0x1) == 0)
        {
            this.areaSizeX = this.imageData.getWidth();
            this.areaSizeZ = this.imageData.getHeight();
        }
        // 90 or 270 degree template rotation
        else
        {
            this.areaSizeX = this.imageData.getHeight();
            this.areaSizeZ = this.imageData.getWidth();
        }
    }

    protected int getTemplateRotation(long posX, long posZ)
    {
        if (this.useTemplateRotation == false)
        {
            return 0;
        }

        rand.setSeed(posX * this.randLong1 + posZ * this.randLong2 ^ this.worldSeed);

        return rand.nextInt(4);
    }

    protected void setTemplateRotation(long posX, long posZ)
    {
        this.templateRotation = this.getTemplateRotation(posX, posZ);
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

    protected int getImageAlphaAt(int imageX, int imageY)
    {
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
            PaintedBiomes.logger.fatal("getImageAlphaAt(): Error reading the alpha channel of the template image; imageX: " +
                                        imageX + " imageY: " + imageY);
        }

        return alpha[0];
    }

    protected abstract int getAreaX(int blockX);

    protected abstract int getAreaZ(int blockZ);

    protected abstract boolean isLocationCoveredByTemplate(int blockX, int blockZ);

    protected boolean isBiomeDefinedByTemplateAt(int areaX, int areaZ)
    {
        int imageX = this.getImageX(areaX, areaZ);
        int imageY = this.getImageY(areaX, areaZ);
        int alpha = this.getImageAlphaAt(imageX, imageY);

        // Completely transparent pixel
        if (alpha == 0x00)
        {
            return this.templateUndefinedAreaBiomeID != -1;
        }

        return ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(imageX, imageY)) != -1;
    }

    protected int getBiomeIdFromTemplateImage(int areaX, int areaZ, int defaultBiomeID)
    {
        int imageX = this.getImageX(areaX, areaZ);
        int imageY = this.getImageY(areaX, areaZ);
        int alpha = this.getImageAlphaAt(imageX, imageY);

        // Completely transparent pixel
        if (alpha == 0x00)
        {
            return this.getUndefinedAreaBiomeID(defaultBiomeID);
        }

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(imageX, imageY));

        return biomeID != -1 ? biomeID : this.getUndefinedAreaBiomeID(defaultBiomeID);
    }

    @Override
    public boolean isBiomeDefinedAt(int blockX, int blockZ)
    {
        if (this.isLocationCoveredByTemplate(blockX, blockZ) == false)
        {
            return this.unpaintedAreaBiomeID != -1;
        }

        return this.isBiomeDefinedByTemplateAt(this.getAreaX(blockX), this.getAreaZ(blockZ));
    }

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.isLocationCoveredByTemplate(blockX, blockZ) == false)
        {
            return this.getUnpaintedAreaBiomeID(defaultBiomeID);
        }

        return this.getBiomeIdFromTemplateImage(this.getAreaX(blockX), this.getAreaZ(blockZ), defaultBiomeID);
    }
}
