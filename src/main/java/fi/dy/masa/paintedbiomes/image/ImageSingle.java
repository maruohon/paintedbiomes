package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingle implements IImageReader
{
    protected int dimension;
    protected File imageFile;
    protected BufferedImage imageData;
    protected int imageWidth;
    protected int imageHeight;

    protected int templateAlignmentMode;
    protected int templateAlignmentX;
    protected int templateAlignmentZ;

    protected int unpaintedAreaBiomeID;
    protected int templateUndefinedAreaBiomeID;

    protected int minX;
    protected int maxX;
    protected int minZ;
    protected int maxZ;

    public ImageSingle(int dimension, File imageFile)
    {
        this.dimension = dimension;
        this.imageFile = imageFile;
        this.reload();
    }

    public void reload()
    {
        this.readImageTemplate(this.imageFile);

        Configs conf = Configs.getConfig(this.dimension);
        this.unpaintedAreaBiomeID = conf.unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = conf.templateUndefinedAreaBiome;
        this.templateAlignmentMode = conf.templateAlignmentMode;
        this.templateAlignmentX = conf.templateAlignmentX;
        this.templateAlignmentZ = conf.templateAlignmentZ;

        this.setBoundsFromImage();
    }

    public void readImageTemplate(File imageFile)
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

    public void setBoundsFromImage()
    {
        if (this.imageData != null)
        {
            this.imageWidth = this.imageData.getWidth();
            this.imageHeight = this.imageData.getHeight();

            // Centered
            if (this.templateAlignmentMode == 0)
            {
                this.minX = this.templateAlignmentX - (this.imageWidth / 2);
                this.minZ = this.templateAlignmentZ - (this.imageHeight / 2);
                this.maxX = this.templateAlignmentX + (int)Math.ceil(((double)this.imageWidth / 2.0d)) - 1;
                this.maxZ = this.templateAlignmentZ + (int)Math.ceil(((double)this.imageHeight / 2.0d)) - 1;
            }
            // The top left corner is at the set coordinates
            else if (this.templateAlignmentMode == 1)
            {
                this.minX = this.templateAlignmentX;
                this.minZ = this.templateAlignmentZ;
                this.maxX = this.minX + this.imageWidth - 1;
                this.maxZ = this.minZ + this.imageHeight - 1;
            }
            // The top right corner is at the set coordinates
            else if (this.templateAlignmentMode == 2)
            {
                this.minX = this.templateAlignmentX - this.imageWidth;
                this.minZ = this.templateAlignmentZ;
                this.maxX = this.templateAlignmentX - 1;
                this.maxZ = this.minZ + this.imageHeight - 1;
            }
            // The bottom right corner is at the set coordinates
            else if (this.templateAlignmentMode == 3)
            {
                this.minX = this.templateAlignmentX - this.imageWidth;
                this.minZ = this.templateAlignmentZ - this.imageHeight;
                this.maxX = this.templateAlignmentX - 1;
                this.maxZ = this.templateAlignmentZ - 1;
            }
            // The bottom left corner is at the set coordinates
            else if (this.templateAlignmentMode == 4)
            {
                this.minX = this.templateAlignmentX;
                this.minZ = this.templateAlignmentZ - this.imageHeight;
                this.maxX = this.minX + this.imageWidth - 1;
                this.maxZ = this.templateAlignmentZ - 1;
            }
        }
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

    protected int getBiomeIdFromTemplateImage(int imageX, int imageY, int defaultBiomeID)
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
            PaintedBiomes.logger.fatal("getBiomeIdFromTemplateImage(): Error reading the alpha channel of the template image - minX: " + minX + " maxX: " + maxX + " minZ: " + minZ + " maxZ: " + maxZ + " x: " + imageX + " y: " + imageY);
        }

        // Completely transparent pixel
        if (alpha[0] == 0x00)
        {
            return this.getUndefinedAreaBiomeID(defaultBiomeID);
        }

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(imageX, imageY));

        return biomeID != -1 ? biomeID : this.getUndefinedAreaBiomeID(defaultBiomeID);
    }

    public boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return this.imageData != null && blockX >= this.minX && blockX <= this.maxX && blockZ >= this.minZ && blockZ <= this.maxZ;
    }

    protected boolean isBiomeDefinedByTemplateAt(int imageX, int imageY)
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
            PaintedBiomes.logger.fatal("isBiomeDefinedByTemplateAt(): Error reading the alpha channel of the template image - minX: " + minX + " maxX: " + maxX + " minZ: " + minZ + " maxZ: " + maxZ + " x: " + imageX + " y: " + imageY);
        }

        // Completely transparent pixel
        if (alpha[0] == 0x00)
        {
            return this.templateUndefinedAreaBiomeID != -1;
        }

        return ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(imageX, imageY)) != -1;
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
