package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public abstract class ImageBase implements IImageReader
{
    protected static final Random rand = new Random();
    protected final long randLong1;
    protected final long randLong2;

    protected final int dimension;
    protected final long worldSeed;

    protected final boolean useAlternateTemplates;
    protected final boolean useTemplateFlipping;
    protected final boolean useTemplateRotation;
    protected final int maxAlternateTemplates;
    protected final int unpaintedAreaBiomeID;
    protected final int templateUndefinedAreaBiomeID;

    protected BufferedImage imageData;
    protected int imageWidth;
    protected int imageHeight;
    protected int areaSizeX;
    protected int areaSizeZ;

    protected int templateRotation;
    protected int templateFlip;
    protected int alternateTemplate;

    public ImageBase(int dimension, long seed)
    {
        this.dimension = dimension;
        this.worldSeed = seed;

        Configs conf = Configs.getConfig(this.dimension);
        this.useAlternateTemplates = conf.useAlternateTemplates;
        this.useTemplateFlipping = conf.useTemplateRandomFlipping;
        this.useTemplateRotation = conf.useTemplateRandomRotation;
        this.maxAlternateTemplates = conf.maxAlternateTemplates;
        this.unpaintedAreaBiomeID = conf.unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = conf.templateUndefinedAreaBiome;

        rand.setSeed(this.worldSeed);
        this.randLong1 = rand.nextLong() / 2L * 2L + 1L;
        this.randLong2 = rand.nextLong() / 2L * 2L + 1L;
    }

    protected BufferedImage readImageData(File templateFile)
    {
        try
        {
            if (templateFile.exists() == true)
            {
                BufferedImage image = ImageIO.read(templateFile);

                if (image != null)
                {
                    PaintedBiomes.logger.info("Successfully read template image from '" + templateFile.getAbsolutePath() + "'");
                    return image;
                }
            }
        }
        catch (IOException e)
        {
            PaintedBiomes.logger.warn("Failed to read template image from '" + templateFile.getAbsolutePath() + "'");
        }

        return null;
    }

    protected void setTemplateDimensions()
    {
        if (this.imageData == null)
        {
            //PaintedBiomes.logger.warn("null template image while trying to get template dimensions");
            return;
        }

        this.imageWidth = this.imageData.getWidth();
        this.imageHeight = this.imageData.getHeight();

        // 0 degree or 180 degree template rotation
        if ((this.templateRotation & 0x1) == 0)
        {
            this.areaSizeX = this.imageWidth;
            this.areaSizeZ = this.imageHeight;
        }
        // 90 or 270 degree template rotation
        else
        {
            this.areaSizeX = this.imageHeight;
            this.areaSizeZ = this.imageWidth;
        }
    }

    protected void setTemplateTransformations(long posX, long posZ)
    {
        rand.setSeed(posX * this.randLong1 + posZ * this.randLong2 ^ this.worldSeed);

        int r = rand.nextInt(4);
        this.templateRotation = this.useTemplateRotation == true ? r : 0;

        r = rand.nextInt(4); // 0x1 = flip image X, 0x2 = flip image Y => 0..3
        this.templateFlip = this.useTemplateFlipping ? r : 0;

        r = rand.nextInt(this.maxAlternateTemplates + 1);
        this.alternateTemplate = this.useAlternateTemplates == true ? r : 0;
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
        int imageX = 0;

        switch (this.templateRotation)
        {
            case 0: // normal (0 degrees) template rotation
                imageX = areaX;
                break;
            case 1: // 90 degree template rotation clock-wise
                imageX = areaZ;
                break;
            case 2: // 180 degree template rotation clock-wise
                imageX = this.areaSizeX - areaX - 1;
                break;
            case 3: // 270 degree template rotation clock-wise
                imageX = this.areaSizeZ - areaZ - 1;
                break;
            default:
        }

        // Flip the template on the X-axis
        if ((this.templateFlip & 0x1) != 0)
        {
            imageX = this.imageWidth - imageX - 1;
        }

        return imageX;
    }

    protected int getImageY(int areaX, int areaZ)
    {
        int imageY = 0;

        switch (this.templateRotation)
        {
            case 0: // normal (0 degrees) template rotation
                imageY = areaZ;
                break;
            case 1: // 90 degree template rotation clock-wise
                imageY = this.areaSizeX - areaX - 1;
                break;
            case 2: // 180 degree template rotation clock-wise
                imageY = this.areaSizeZ - areaZ - 1;
                break;
            case 3: // 270 degree template rotation clock-wise
                imageY = areaX;
                break;
            default:
        }

        // Flip the template on the Y-axis
        if ((this.templateFlip & 0x2) != 0)
        {
            imageY = this.imageHeight - imageY - 1;
        }

        return imageY;
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
