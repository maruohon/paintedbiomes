package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.world.biome.BiomeGenBase;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingle implements IImageReader
{
    private ColorToBiomeMapping colorToBiome;
    private File imageFile;
    private BufferedImage imageData;
    private int imageWidth;
    private int imageHeight;

    private int templateAlignmentMode;
    private int templateAlignmentX;
    private int templateAlignmentZ;

    private int unpaintedAreaBiomeID;
    private int templateUndefinedAreaBiomeID;
    private BiomeGenBase unpaintedAreaBiome;
    private BiomeGenBase templateUndefinedAreaBiome;

    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;

    public ImageSingle(File imageFile)
    {
        this.imageFile = imageFile;
        this.colorToBiome = ColorToBiomeMapping.getInstance();
        this.reload();
    }

    public void reload()
    {
        this.readImageTemplate(this.imageFile);
        this.setTemplateAlignment(Configs.getInstance().templateAlignmentMode, Configs.getInstance().templateAlignmentX, Configs.getInstance().templateAlignmentZ);
        this.setBoundsFromImage();
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
                PaintedBiomes.logger.info("Successfully read single-image-template from '" + imageFile.getAbsolutePath() + "'");
            }
        }
        catch (IOException e)
        {
            PaintedBiomes.logger.warn("Failed to read single-image-template from '" + imageFile.getAbsolutePath() + "'");
        }
    }

    public void setTemplateAlignment(int alignmentMode, int x, int z)
    {
        this.templateAlignmentMode = alignmentMode;
        this.templateAlignmentX = x;
        this.templateAlignmentZ = z;
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
        return this.imageData != null && blockX >= this.minX && blockX <= this.maxX && blockZ >= this.minZ && blockZ <= this.maxZ;
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

        int x = blockX - this.minX;
        int y = blockZ - this.minZ;

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
