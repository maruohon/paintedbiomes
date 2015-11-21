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
    protected String name;
    protected File imageFile;

    protected BufferedImage imageData;
    protected int imageWidth;
    protected int imageHeight;

    protected int unpaintedAreaBiomeID;
    protected int templateUndefinedAreaBiomeID;

    public ImageRegion(int regionX, int regionZ, String path)
    {
        this.name = "r." + regionX + "." + regionZ;
        this.imageFile = new File(path, this.name + ".png");

        this.readImageTemplate(this.imageFile);
        this.unpaintedAreaBiomeID = Configs.getInstance().unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = Configs.getInstance().templateUndefinedAreaBiome;
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
                    //PaintedBiomes.logger.info("Successfully read image template for region '" + this.name + "' from '" + imageFile.getAbsolutePath() + "'");

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

    public boolean areCoordinatesInsideTemplate(int blockX, int blockZ)
    {
        blockX = (blockX % 512 + 512) % 512;
        blockZ = (blockZ % 512 + 512) % 512;

        return this.imageData != null && blockX < this.imageWidth && blockZ < this.imageHeight;
    }

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.areCoordinatesInsideTemplate(blockX, blockZ) == false)
        {
            // Default biome defined for areas outside of the template image
            if (this.unpaintedAreaBiomeID != -1)
            {
                return this.unpaintedAreaBiomeID;
            }

            return defaultBiomeID;
        }

        //System.out.println("ImageRegion.getBiomeAt(" + blockX + ", " + blockZ + ")");
        int x = (blockX % 512 + 512) % 512;
        int y = (blockZ % 512 + 512) % 512;

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.imageData.getRGB(x, y));

        // Undefined color mapping, use either the templateUndefinedAreaBiome or the default biome from the terrain generator
        if (biomeID == -1)
        {
            return this.getUndefinedAreaBiomeID(defaultBiomeID);
        }

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

        // Completely transparent pixel, use either the templateUndefinedAreaBiome or the default biome from the terrain generator
        if (alpha[0] == 0x00)
        {
            return this.getUndefinedAreaBiomeID(defaultBiomeID);
        }

        return biomeID;
    }

    protected int getUndefinedAreaBiomeID(int defaultBiomeID)
    {
        // Return the Biome ID for the undefined areas, if one has been set, otherwise return the one from the regular terrain generation
        return this.templateUndefinedAreaBiomeID != -1 ? this.templateUndefinedAreaBiomeID : defaultBiomeID;
    }
}
