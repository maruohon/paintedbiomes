package fi.dy.masa.paintedbiomes.image;

import java.io.File;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageRegion extends ImageBase implements IImageReader
{
    protected final String name;
    protected final int regionX;
    protected final int regionZ;

    public ImageRegion(int dimension, int regionX, int regionZ, long seed, Configs config, File templatePath)
    {
        super(dimension, seed, config);

        this.regionX = regionX;
        this.regionZ = regionZ;
        this.name = "r." + regionX + "." + regionZ;

        this.setTemplateTransformations(this.regionX, this.regionZ);
        this.readTemplateImage(templatePath);
    }

    protected void readTemplateImage(File templatePath)
    {
        File templateFile = new File(templatePath, this.name + ".png");

        if (this.useAlternateTemplates)
        {
            File tmpFile = new File(templatePath, this.name + "_alt_" + (this.alternateTemplate + 1) + ".png");

            if (tmpFile.exists() && tmpFile.isFile())
            {
                templateFile = tmpFile;
            }
        }

        this.imageData = this.readImageData(templateFile);
        this.setTemplateDimensions();
    }

    @Override
    protected void setTemplateDimensions()
    {
        super.setTemplateDimensions();

        // non-square template image while random template rotation is enabled...
        if (this.useTemplateRotation && this.areaSizeX != this.areaSizeZ)
        {
            String str = String.format("*** WARNING: Template random rotations enabled, but the template image for " +
                    "region r.%d.%d is not square! Clipping the template to a square!", this.regionX, this.regionZ);
            PaintedBiomes.logger.warn(str);

            // Clip the template image to a square
            this.areaSizeX = Math.min(this.areaSizeX, this.areaSizeZ);
            this.areaSizeZ = Math.min(this.areaSizeX, this.areaSizeZ);
        }
    }

    @Override
    protected int getAreaX(int blockX)
    {
        return blockX & 0x1FF;
        //return (blockX % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
    }

    @Override
    protected int getAreaZ(int blockZ)
    {
        return blockZ & 0x1FF;
        //return (blockZ % RegionCoords.REGION_SIZE + RegionCoords.REGION_SIZE) % RegionCoords.REGION_SIZE;
    }

    @Override
    protected boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return this.imageData != null && this.getAreaX(blockX) < this.areaSizeX && this.getAreaZ(blockZ) < this.areaSizeZ;
    }
}
