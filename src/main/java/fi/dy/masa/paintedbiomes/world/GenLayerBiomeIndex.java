package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerBiome;
import fi.dy.masa.paintedbiomes.image.ImageHandler;

public class GenLayerBiomeIndex extends GenLayerBiome
{
    public GenLayerBiomeIndex(long seed, GenLayer parent, WorldType worldType)
    {
        super(seed, parent, worldType);
    }

    @Override
    public int[] getInts(int x, int z, int width, int length)
    {
        int[] ints = this.parent.getInts(x, z, width, length);

        int i = 0;
        int endX = x + width;
        int endZ = z + length;

        for (int tz = z; tz < endZ; ++tz)
        {
            for (int tx = x; tx < endX; ++tx)
            {
                // FIXME is there any way to have per-dimension GenLayers?
                ints[i] = ImageHandler.getImageHandler(0).getBiomeIDAt(tx, tz, ints[i]);
                i++;
            }
        }

        return ints;
    }
}
