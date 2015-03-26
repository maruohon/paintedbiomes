package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.world.biome.BiomeGenBase;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageRegion implements IImageReader
{
    private ColorToBiomeMapping colorToBiome;
    private String name;
    private File imageFile;

    private BufferedImage imageData;
    private int imageWidth;
    private int imageHeight;

    private int unpaintedAreaBiomeID;
    private int templateUndefinedAreaBiomeID;
    private BiomeGenBase unpaintedAreaBiome;
    private BiomeGenBase templateUndefinedAreaBiome;

    public ImageRegion(int regionX, int regionZ, String path)
    {
        this.name = "r." + regionX + "." + regionZ;
        this.imageFile = new File(path, this.name + ".png");
        this.colorToBiome = ColorToBiomeMapping.getInstance();

        this.readImageTemplate(this.imageFile);
        this.setBiomeHandling();
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

    public void setBiomeHandling()
    {
        this.unpaintedAreaBiomeID = Configs.getInstance().unpaintedAreaBiome;
        this.templateUndefinedAreaBiomeID = Configs.getInstance().templateUndefinedAreaBiome;

        if (this.unpaintedAreaBiomeID >= 0 && this.unpaintedAreaBiomeID <= 255)
        {
            this.unpaintedAreaBiome = BiomeGenBase.getBiome(this.unpaintedAreaBiomeID);
        }

        if (this.templateUndefinedAreaBiomeID >= 0 && this.templateUndefinedAreaBiomeID <= 255)
        {
            this.templateUndefinedAreaBiome = BiomeGenBase.getBiome(this.templateUndefinedAreaBiomeID);
        }
    }

    @Override
    public boolean areCoordinatesInsideTemplate(int blockX, int blockZ)
    {
        blockX = (blockX % 512 + 512) % 512;
        blockZ = (blockZ % 512 + 512) % 512;
        //System.out.println("areCoordinatesInsideTemplate(): " + (this.imageData != null && blockX < this.imageWidth && blockZ < this.imageHeight));
        return this.imageData != null && blockX < this.imageWidth && blockZ < this.imageHeight;
    }

    @Override
    public BiomeGenBase getBiomeAt(int blockX, int blockZ, BiomeGenBase defaultBiome)
    {
        if (this.areCoordinatesInsideTemplate(blockX, blockZ) == false)
        {
            // Default biome defined for areas outside of the template image
            if (this.unpaintedAreaBiomeID >= 0 && this.templateUndefinedAreaBiomeID <= 255)
            {
                return this.unpaintedAreaBiome;
            }

            return defaultBiome;
        }

        //System.out.println("ImageRegion.getBiomeAt(" + blockX + ", " + blockZ + ")");
        int x = (blockX % 512 + 512) % 512;
        int y = (blockZ % 512 + 512) % 512;

        BiomeGenBase biome = this.colorToBiome.getBiomeForColor(this.imageData.getRGB(x, y));
        int[] alpha = new int[1];
        try
        {
            this.imageData.getAlphaRaster().getPixel(x, y, alpha);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            PaintedBiomes.logger.fatal("Error reading the alpha channel of the template image");
        }

        // Completely transparent pixel or undefined color mapping, use either the templateUndefinedAreaBiome or the default biome from the terrain generator
        if (alpha[0] == 0x00 || biome == null)
        {
            // Default biome defined for transparent areas
            if (this.templateUndefinedAreaBiomeID >= 0 && this.templateUndefinedAreaBiomeID <= 255)
            {
                return this.templateUndefinedAreaBiome;
            }

            return defaultBiome;
        }

        return biome;
    }
}
