package fi.dy.masa.paintedbiomes.image;

import java.io.File;

import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingleRepeating extends ImageSingle
{
    public static final int NEGX = 0x01;
    public static final int NEGZ = 0x04;
    public static final int POSX = 0x02;
    public static final int POSZ = 0x08;

    protected int repeatTemplate;
    protected int repeatEdge;

    public ImageSingleRepeating(File imageFile)
    {
        super(imageFile);
    }

    @Override
    public void reload()
    {
        super.reload();

        Configs conf = Configs.getInstance();

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

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        int area = this.getArea(blockX, blockZ);

        if (this.imageData == null)
        {
            return this.getUnpaintedAreaBiomeID(defaultBiomeID);
        }

        // The given coordinates are covered by a template image
        if (area == 0)
        {
            return this.getBiomeIdFromTemplateImage(blockX - this.minX, blockZ - this.minZ, defaultBiomeID);
        }

        // The given coordinates are not covered by a template image, figure out if there is a valid repeating option for the given location

        // Template repeating enabled, and the given location is covered by the repeat setting.
        // Note: This means that either the area is on one of the sides from the template, (ie. inside the template's coverage
        // on one axis), or that both of the sides adjacent to the corner that the location is in, have repeating enabled.
        if (this.repeatTemplate != 0 && (this.repeatTemplate & area) == area)
        {
            int x = ((blockX - this.minX) % this.imageWidth + this.imageWidth) % this.imageWidth;
            int z = ((blockZ - this.minZ) % this.imageHeight + this.imageHeight) % this.imageHeight;
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
                x = this.imageWidth - 1;
            }

            if (blockZ < this.minZ)
            {
                z = 0;
            }
            else if (blockZ > this.maxZ)
            {
                z = this.imageHeight - 1;
            }

            return this.getBiomeIdFromTemplateImage(x, z, defaultBiomeID);
        }

        // If there is a biome defined for unpainted areas, then use that, otherwise use the biome from the regular terrain generation
        return this.getUnpaintedAreaBiomeID(defaultBiomeID);
    }
}
