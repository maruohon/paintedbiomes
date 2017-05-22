package fi.dy.masa.paintedbiomes.image;

import java.io.File;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingle extends ImageBase implements IImageReader
{
    protected final File templatePath;
    protected final int templateAlignmentMode;
    protected final int templateAlignmentX;
    protected final int templateAlignmentZ;

    protected int minX;
    protected int maxX;
    protected int minZ;
    protected int maxZ;

    public ImageSingle(int dimension, long seed, File templatePath)
    {
        super(dimension, seed);

        Configs conf = Configs.getConfig(this.dimension);
        this.templateAlignmentMode = conf.templateAlignmentMode;
        this.templateAlignmentX = conf.templateAlignmentX;
        this.templateAlignmentZ = conf.templateAlignmentZ;
        this.templatePath = templatePath;
    }

    public void init()
    {
        this.setTemplateTransformations(this.templateAlignmentX, this.templateAlignmentZ);
        this.readTemplateImage(this.templatePath);
    }

    protected void readTemplateImage(File templatePath)
    {
        File templateFile = new File(templatePath, "biomes.png");

        if (this.useAlternateTemplates)
        {
            File tmpFile = new File(templatePath, "biomes_alt_" + (this.alternateTemplate + 1) + ".png");

            if (tmpFile.exists() && tmpFile.isFile())
            {
                templateFile = tmpFile;
            }
        }

        this.imageData = this.readImageData(templateFile);
        this.setTemplateDimensions();
        this.setAreaBounds();
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

    @Override
    protected int getAreaX(int blockX)
    {
        return blockX - this.minX;
    }

    @Override
    protected int getAreaZ(int blockZ)
    {
        return blockZ - this.minZ;
    }

    @Override
    protected boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return this.imageData != null && blockX >= this.minX && blockX <= this.maxX && blockZ >= this.minZ && blockZ <= this.maxZ;
    }
}
