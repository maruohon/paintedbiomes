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

    protected int repeatTemplate;
    protected int repeatEdge;

    public ImageSingleRepeating(int dimension, long seed, File imageFile)
    {
        super(dimension, seed, imageFile);
    }

    @Override
    protected void reload()
    {
        super.reload();

        // non-square template image with random template rotations enabled...
        if (this.useTemplateRotation == true && this.areaSizeX != this.areaSizeZ)
        {
            PaintedBiomes.logger.warn("*** WARNING: Template random rotations enabled, but the template image is not square! Clipping the template to a square!");

            // Clip the template image to a square
            this.areaSizeX = Math.min(this.areaSizeX, this.areaSizeZ);
            this.areaSizeZ = Math.min(this.areaSizeX, this.areaSizeZ);
            this.setAreaBounds();
        }

        Configs conf = Configs.getConfig(this.dimension);

        this.repeatTemplate = 0;
        this.repeatEdge = 0;

        if (conf.repeatTemplatePositiveX == 1) this.repeatTemplate |= POSX;
        if (conf.repeatTemplatePositiveZ == 1) this.repeatTemplate |= POSZ;
        if (conf.repeatTemplateNegativeX == 1) this.repeatTemplate |= NEGX;
        if (conf.repeatTemplateNegativeZ == 1) this.repeatTemplate |= NEGZ;

        if (conf.repeatTemplatePositiveX == 2) this.repeatEdge |= POSX;
        if (conf.repeatTemplatePositiveZ == 2) this.repeatEdge |= POSZ;
        if (conf.repeatTemplateNegativeX == 2) this.repeatEdge |= NEGX;
        if (conf.repeatTemplateNegativeZ == 2) this.repeatEdge |= NEGZ;
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

    protected int getTemplateRotation(long posX, long posZ, boolean checkAdjacent)
    {
        if (this.useTemplateRotation == false)
        {
            return 0;
        }

        rand.setSeed(posX * this.randLong1 + posZ * this.randLong2 ^ this.worldSeed);
        return rand.nextInt(4);
    }

    protected void setTemplateRotation(long posX, long posZ)
    {
        this.templateRotation = this.getTemplateRotation(posX, posZ, true);
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
            this.setTemplateRotation(0, 0);

            return this.isBiomeDefinedByTemplateAt(blockX - this.minX, blockZ - this.minZ);
        }

        // The given coordinates are not covered by a template image, figure out if there is a valid repeating option for the given location

        // Template repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatTemplate != 0 && (this.repeatTemplate & area) == area)
        {
            int x = ((blockX - this.minX) % this.areaSizeX + this.areaSizeX) % this.areaSizeX;
            int y = ((blockZ - this.minZ) % this.areaSizeZ + this.areaSizeZ) % this.areaSizeZ;

            // Repeated template, use a random rotation based on the relative position of the repeated template
            int tx = (int)Math.floor(((float)blockX - (float)this.minX) / (float)this.areaSizeX);
            int tz = (int)Math.floor(((float)blockZ - (float)this.minZ) / (float)this.areaSizeZ);
            this.setTemplateRotation(tx, tz);

            return this.isBiomeDefinedByTemplateAt(x, y);
        }

        // Template edge repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatEdge != 0 && (this.repeatEdge & area) == area)
        {
            int x = blockX - this.minX;
            int y = blockZ - this.minZ;

            if (blockX < this.minX)
            {
                x = 0;
            }
            else if (blockX > this.maxX)
            {
                x = this.areaSizeX - 1;
            }

            if (blockZ < this.minZ)
            {
                y = 0;
            }
            else if (blockZ > this.maxZ)
            {
                y = this.areaSizeZ - 1;
            }

            // use a rotation based on the template alignment position
            this.setTemplateRotation(0, 0);

            return this.isBiomeDefinedByTemplateAt(x, y);
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
            this.setTemplateRotation(0, 0);

            return this.getBiomeIdFromTemplateImage(blockX - this.minX, blockZ - this.minZ, defaultBiomeID);
        }

        // The given coordinates are not covered by a template image, figure out if there is a valid repeating option for the given location

        // Template repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatTemplate != 0 && (this.repeatTemplate & area) == area)
        {
            int x = ((blockX - this.minX) % this.areaSizeX + this.areaSizeX) % this.areaSizeX;
            int z = ((blockZ - this.minZ) % this.areaSizeZ + this.areaSizeZ) % this.areaSizeZ;

            // Repeated template, use a random rotation based on the relative position of the repeated template
            int tx = (int)Math.floor(((float)blockX - (float)this.minX) / (float)this.areaSizeX);
            int tz = (int)Math.floor(((float)blockZ - (float)this.minZ) / (float)this.areaSizeZ);
            this.setTemplateRotation(tx, tz);

            return this.getBiomeIdFromTemplateImage(x, z, defaultBiomeID);
        }

        // Template edge repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatEdge != 0 && (this.repeatEdge & area) == area)
        {
            int x = blockX - this.minX;
            int z = blockZ - this.minZ;

            if (blockX < this.minX)
            {
                x = 0;
            }
            else if (blockX > this.maxX)
            {
                x = this.areaSizeX - 1;
            }

            if (blockZ < this.minZ)
            {
                z = 0;
            }
            else if (blockZ > this.maxZ)
            {
                z = this.areaSizeZ - 1;
            }

            // use a rotation based on the template alignment position
            this.setTemplateRotation(0, 0);

            return this.getBiomeIdFromTemplateImage(x, z, defaultBiomeID);
        }

        // If there is a biome defined for unpainted areas, then use that, otherwise use the biome from the regular terrain generation
        return this.getUnpaintedAreaBiomeID(defaultBiomeID);
    }
}
