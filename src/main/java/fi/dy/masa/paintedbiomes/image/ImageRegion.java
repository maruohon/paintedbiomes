package fi.dy.masa.paintedbiomes.image;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;

public class ImageRegion extends ImageBase implements IImageReader
{
    protected static final Random rand = new Random();
    protected final String name;
    protected final int regionX;
    protected final int regionZ;

    public ImageRegion(int dimension, int regionX, int regionZ, long seed, File path)
    {
        super(dimension, seed);

        this.regionX = regionX;
        this.regionZ = regionZ;
        this.name = "r." + regionX + "." + regionZ;

        this.readImageTemplate(new File(path, this.name + ".png"));
    }

    protected void readImageTemplate(File imageFile)
    {
        try
        {
            if (imageFile.exists() == true)
            {
                this.imageData = ImageIO.read(imageFile);

                if (this.imageData != null)
                {
                    PaintedBiomes.logger.info("Successfully read image template for region '" +
                            this.name + "' from '" + imageFile.getAbsolutePath() + "'");
                    this.setTemplateDimensions();
                    this.setTemplateRotation(this.regionX, this.regionZ);
                }
            }
        }
        catch (IOException e)
        {
            //PaintedBiomes.logger.warn("Failed to read image template for region '" + this.name + "' from '" + imageFile.getAbsolutePath() + "'");
        }
    }

    @Override
    protected void setTemplateDimensions()
    {
        super.setTemplateDimensions();

        // non-square template image while random template rotation is enabled...
        if (this.useTemplateRotation == true && this.areaSizeX != this.areaSizeZ)
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
