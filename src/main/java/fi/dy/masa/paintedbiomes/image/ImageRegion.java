package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageRegion implements IImageReader
{
    protected int dimension;
    protected String name;

    protected BufferedImage imageData;
    protected int imageWidth;
    protected int imageHeight;

    protected int unpaintedAreaBiomeID;
    protected int templateUndefinedAreaBiomeID;

    public ImageRegion(int dimension, int regionX, int regionZ, File path)
    {
        this.dimension = dimension;
        this.name = "r." + regionX + "." + regionZ;

        this.readImageTemplate(new File(path, this.name + ".png"));
        this.unpaintedAreaBiomeID = Configs.getConfig(this.dimension).unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = Configs.getConfig(this.dimension).templateUndefinedAreaBiome;
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
                    this.imageWidth = this.imageData.getWidth();
                    this.imageHeight = this.imageData.getHeight();
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

    protected int getBiomeIdFromTemplateImage(int blockX, int blockZ, int defaultBiomeID)
    {
        //int x = (blockX % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
        //int y = (blockZ % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
        int x = blockX & 0x1FF;
        int y = blockZ & 0x1FF;
        int[] alpha = new int[1];

        try
        {
            WritableRaster raster = this.imageData.getAlphaRaster();
            if (raster != null)
            {
                raster.getPixel(x, y, alpha);
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

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(x, y));

        return biomeID != -1 ? biomeID : this.getUndefinedAreaBiomeID(defaultBiomeID);
    }

    public boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        //int x = (blockX % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
        //int z = (blockZ % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
        int x = blockX & 0x1FF;
        int z = blockZ & 0x1FF;

        return this.imageData != null && x < this.imageWidth && z < this.imageHeight;
    }

    @Override
    public boolean isBiomeDefinedAt(int blockX, int blockZ)
    {
        if (this.isLocationCoveredByTemplate(blockX, blockZ) == false)
        {
            return this.unpaintedAreaBiomeID != -1;
        }

        //int x = (blockX % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
        //int y = (blockZ % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
        int x = blockX & 0x1FF;
        int y = blockZ & 0x1FF;
        int[] alpha = new int[1];

        try
        {
            WritableRaster raster = this.imageData.getAlphaRaster();
            if (raster != null)
            {
                raster.getPixel(x, y, alpha);
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

        return ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(x, y)) != -1;
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
