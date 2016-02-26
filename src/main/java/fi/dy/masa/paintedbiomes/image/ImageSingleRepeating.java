package fi.dy.masa.paintedbiomes.image;

import java.io.File;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingleRepeating extends ImageSingle
{
    public static final int NEGX = 0x01;
    public static final int NEGZ = 0x04;
    public static final int POSX = 0x02;
    public static final int POSZ = 0x08;

    protected final int repeatTemplate;
    protected final int repeatEdge;

    public ImageSingleRepeating(int dimension, long seed, File imageFile)
    {
        super(dimension, seed, imageFile);

        Configs conf = Configs.getConfig(this.dimension);

        int repeatTemplate = 0;
        if (conf.repeatTemplatePositiveX == 1) repeatTemplate |= POSX;
        if (conf.repeatTemplatePositiveZ == 1) repeatTemplate |= POSZ;
        if (conf.repeatTemplateNegativeX == 1) repeatTemplate |= NEGX;
        if (conf.repeatTemplateNegativeZ == 1) repeatTemplate |= NEGZ;

        int repeatEdge = 0;
        if (conf.repeatTemplatePositiveX == 2) repeatEdge |= POSX;
        if (conf.repeatTemplatePositiveZ == 2) repeatEdge |= POSZ;
        if (conf.repeatTemplateNegativeX == 2) repeatEdge |= NEGX;
        if (conf.repeatTemplateNegativeZ == 2) repeatEdge |= NEGZ;

        this.repeatTemplate = repeatTemplate;
        this.repeatEdge = repeatEdge;
    }

    @Override
    protected void setTemplateDimensions()
    {
        super.setTemplateDimensions();

        // non-square template image while random template rotation is enabled...
        if (this.useTemplateRotation == true && this.areaSizeX != this.areaSizeZ)
        {
            PaintedBiomes.logger.warn("*** WARNING: Template random rotations enabled, but the template image is not square!" +
                                      " Clipping the template to a square!");

            // Clip the template image to a square
            this.areaSizeX = Math.min(this.areaSizeX, this.areaSizeZ);
            this.areaSizeZ = Math.min(this.areaSizeX, this.areaSizeZ);
        }
    }

    protected int getArea(int blockX, int blockZ)
    {
        int area = 0;

        if (blockX < this.minX)
        {
            area |= NEGX;
        }
        else if (blockX > this.maxX)
        {
            area |= POSX;
        }

        if (blockZ < this.minZ)
        {
            area |= NEGZ;
        }
        else if (blockZ > this.maxZ)
        {
            area |= POSZ;
        }

        return area;
    }

    @Override
    public boolean isBiomeDefinedAt(int blockX, int blockZ)
    {
        if (this.imageData == null)
        {
            return this.unpaintedAreaBiomeID != -1;
        }

        int area = this.getArea(blockX, blockZ);

        // The given coordinates are covered by a template image
        if (area == 0)
        {
            // Inside the "normal" or "master" template area; use a rotation based on the template alignment position
            this.setTemplateTransformations(0, 0);

            return this.isBiomeDefinedByTemplateAt(blockX - this.minX, blockZ - this.minZ);
        }

        // The given coordinates are not covered by a template image, figure out if there is a valid repeating option for the given location

        // Template repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatTemplate != 0 && (this.repeatTemplate & area) == area)
        {
            int areaX = ((blockX - this.minX) % this.areaSizeX + this.areaSizeX) % this.areaSizeX;
            int areaZ = ((blockZ - this.minZ) % this.areaSizeZ + this.areaSizeZ) % this.areaSizeZ;

            // Repeated template, use a random rotation based on the relative position of the repeated template
            int tx = (int)Math.floor(((float)blockX - (float)this.minX) / (float)this.areaSizeX);
            int tz = (int)Math.floor(((float)blockZ - (float)this.minZ) / (float)this.areaSizeZ);
            this.setTemplateTransformations(tx, tz);

            return this.isBiomeDefinedByTemplateAt(areaX, areaZ);
        }

        // Template edge repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatEdge != 0 && (this.repeatEdge & area) == area)
        {
            int areaX = blockX - this.minX;
            int areaZ = blockZ - this.minZ;

            if (blockX < this.minX)
            {
                areaX = 0;
            }
            else if (blockX > this.maxX)
            {
                areaX = this.areaSizeX - 1;
            }

            if (blockZ < this.minZ)
            {
                areaZ = 0;
            }
            else if (blockZ > this.maxZ)
            {
                areaZ = this.areaSizeZ - 1;
            }

            // use a rotation based on the template alignment position
            this.setTemplateTransformations(0, 0);

            return this.isBiomeDefinedByTemplateAt(areaX, areaZ);
        }

        return this.unpaintedAreaBiomeID != -1;
    }

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.imageData == null)
        {
            return this.getUnpaintedAreaBiomeID(defaultBiomeID);
        }

        int area = this.getArea(blockX, blockZ);

        // The given coordinates are covered by a template image
        if (area == 0)
        {
            // Inside the "normal" or "master" template area; use a rotation based on the template alignment position
            this.setTemplateTransformations(0, 0);

            return this.getBiomeIdFromTemplateImage(blockX - this.minX, blockZ - this.minZ, defaultBiomeID);
        }

        // The given coordinates are not covered by a template image, figure out if there is a valid repeating option for the given location

        // Template repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatTemplate != 0 && (this.repeatTemplate & area) == area)
        {
            int areaX = ((blockX - this.minX) % this.areaSizeX + this.areaSizeX) % this.areaSizeX;
            int areaZ = ((blockZ - this.minZ) % this.areaSizeZ + this.areaSizeZ) % this.areaSizeZ;

            // Repeated template, use a random rotation based on the relative position of the repeated template
            int tx = (int)Math.floor(((float)blockX - (float)this.minX) / (float)this.areaSizeX);
            int tz = (int)Math.floor(((float)blockZ - (float)this.minZ) / (float)this.areaSizeZ);
            this.setTemplateTransformations(tx, tz);

            return this.getBiomeIdFromTemplateImage(areaX, areaZ, defaultBiomeID);
        }

        // Template edge repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatEdge != 0 && (this.repeatEdge & area) == area)
        {
            int areaX = blockX - this.minX;
            int areaZ = blockZ - this.minZ;

            if (blockX < this.minX)
            {
                areaX = 0;
            }
            else if (blockX > this.maxX)
            {
                areaX = this.areaSizeX - 1;
            }

            if (blockZ < this.minZ)
            {
                areaZ = 0;
            }
            else if (blockZ > this.maxZ)
            {
                areaZ = this.areaSizeZ - 1;
            }

            // use a rotation based on the template alignment position
            this.setTemplateTransformations(0, 0);

            return this.getBiomeIdFromTemplateImage(areaX, areaZ, defaultBiomeID);
        }

        // If there is a biome defined for unpainted areas, then use that, otherwise use the biome from the regular terrain generation
        return this.getUnpaintedAreaBiomeID(defaultBiomeID);
    }
}
