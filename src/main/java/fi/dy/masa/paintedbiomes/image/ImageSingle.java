package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingle implements IImageReader
{
    protected final int dimension;
    protected final long worldSeed;
    protected final File imageFile;
    protected final Random rand;
    protected BufferedImage imageData;
    protected int areaSizeX;
    protected int areaSizeZ;

    protected boolean useTemplateRotation;
    protected int templateRotation;
    protected int templateAlignmentMode;
    protected int templateAlignmentX;
    protected int templateAlignmentZ;

    protected int unpaintedAreaBiomeID;
    protected int templateUndefinedAreaBiomeID;

    protected int minX;
    protected int maxX;
    protected int minZ;
    protected int maxZ;

    public ImageSingle(int dimension, long seed, File imageFile)
    {
        this.dimension = dimension;
        this.worldSeed = seed;
        this.imageFile = imageFile;
        this.rand = new Random();
        this.reload();
    }

    protected void reload()
    {
        this.readImageTemplate(this.imageFile);

        Configs conf = Configs.getConfig(this.dimension);
        this.useTemplateRotation = conf.useTemplateRandomRotation;
        this.unpaintedAreaBiomeID = conf.unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = conf.templateUndefinedAreaBiome;
        this.templateAlignmentMode = conf.templateAlignmentMode;
        this.templateAlignmentX = conf.templateAlignmentX;
        this.templateAlignmentZ = conf.templateAlignmentZ;

        this.templateRotation = this.getTemplateRandomRotation(this.templateAlignmentX, this.templateAlignmentZ);
        this.getTemplateDimensions();
        this.setAreaBounds();
    }

    protected void readImageTemplate(File imageFile)
    {
        this.imageData = null;

        try
        {
            if (imageFile.exists() == true)
            {
                this.imageData = ImageIO.read(imageFile);
                PaintedBiomes.logger.info("Successfully read single-image-template from '" + imageFile.getAbsolutePath() + "'");
            }
        }
        catch (IOException e)
        {
            PaintedBiomes.logger.warn("Failed to read single-image-template from '" + imageFile.getAbsolutePath() + "'");
        }
    }

    protected void getTemplateDimensions()
    {
        if (this.imageData == null)
        {
            PaintedBiomes.logger.warn("Failed to get area bounds from template image");
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

    protected void setAreaBounds()
    {
        // Centered
        if (this.templateAlignmentMode == 0)
        {
            this.minX = this.templateAlignmentX - (this.areaSizeX / 2);
            this.minZ = this.templateAlignmentZ - (this.areaSizeZ / 2);
            this.maxX = this.templateAlignmentX + (int)Math.ceil(((double)this.areaSizeX / 2.0d)) - 1;
            this.maxZ = this.templateAlignmentZ + (int)Math.ceil(((double)this.areaSizeZ / 2.0d)) - 1;
        }
        // The top left corner is at the set coordinates
        else if (this.templateAlignmentMode == 1)
        {
            this.minX = this.templateAlignmentX;
            this.minZ = this.templateAlignmentZ;
            this.maxX = this.minX + this.areaSizeX - 1;
            this.maxZ = this.minZ + this.areaSizeZ - 1;
        }
        // The top right corner is at the set coordinates
        else if (this.templateAlignmentMode == 2)
        {
            this.minX = this.templateAlignmentX - this.areaSizeX;
            this.minZ = this.templateAlignmentZ;
            this.maxX = this.templateAlignmentX - 1;
            this.maxZ = this.minZ + this.areaSizeZ - 1;
        }
        // The bottom right corner is at the set coordinates
        else if (this.templateAlignmentMode == 3)
        {
            this.minX = this.templateAlignmentX - this.areaSizeX;
            this.minZ = this.templateAlignmentZ - this.areaSizeZ;
            this.maxX = this.templateAlignmentX - 1;
            this.maxZ = this.templateAlignmentZ - 1;
        }
        // The bottom left corner is at the set coordinates
        else if (this.templateAlignmentMode == 4)
        {
            this.minX = this.templateAlignmentX;
            this.minZ = this.templateAlignmentZ - this.areaSizeZ;
            this.maxX = this.minX + this.areaSizeX - 1;
            this.maxZ = this.templateAlignmentZ - 1;
        }
    }

    protected int getTemplateRandomRotation(long posX, long posZ)
    {
        if (this.useTemplateRotation == false)
        {
            return 0;
        }

        this.rand.setSeed(this.worldSeed);
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(posX * l1 + posZ * l2 ^ this.worldSeed);

        return this.rand.nextInt(4);
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
            return this.areaSizeZ - areaZ - 1;
        }

        // 180 degree template rotation clock-wise
        if (this.templateRotation == 2)
        {
            return this.areaSizeX - areaX - 1;
        }

        // 270 degree template rotation clock-wise
        return areaZ;
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
            PaintedBiomes.logger.fatal("getImageAlphaAt(): Error reading the alpha channel of the template image; minX: " +
                    minX + " maxX: " + maxX + " minZ: " + minZ + " maxZ: " + maxZ + " imageX: " + imageX + " imageY: " + imageY);
        }

        return alpha[0];
    }

    public boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return this.imageData != null && blockX >= this.minX && blockX <= this.maxX && blockZ >= this.minZ && blockZ <= this.maxZ;
    }

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

        return this.isBiomeDefinedByTemplateAt(blockX - this.minX, blockZ - this.minZ);
    }

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.isLocationCoveredByTemplate(blockX, blockZ) == false)
        {
            return this.getUnpaintedAreaBiomeID(defaultBiomeID);
        }

        return this.getBiomeIdFromTemplateImage(blockX - this.minX, blockZ - this.minZ, defaultBiomeID);
    }
}
