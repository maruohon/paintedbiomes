package fi.dy.masa.paintedbiomes.image;

import java.util.HashMap;
import java.util.Map;

import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.util.RegionCoords;

public class ImageCache
{
    public static ImageCache instance;
    public Map<RegionCoords, ImageRegion> imageMap;

    public ImageCache()
    {
        instance = this;
        this.imageMap = new HashMap<RegionCoords, ImageRegion>();
    }

    public boolean contains(int blockX, int blockZ)
    {
        return this.imageMap.containsKey(RegionCoords.fromBlockCoords(blockX, blockZ));
    }

    public ImageRegion getImageRegion(int blockX, int blockZ)
    {
        RegionCoords rc = RegionCoords.fromBlockCoords(blockX, blockZ);
        if (this.imageMap.containsKey(rc) == false)
        {
            //System.out.println("new ImageRegion(" + rc.regionX + ", " + rc.regionZ + ")");
            this.imageMap.put(rc, new ImageRegion(rc.regionX, rc.regionZ, Configs.instance.imagePath));
        }

        return this.imageMap.get(rc);
    }

    public void loadRange(int blockX, int blockZ, int width, int length)
    {
        int startX = blockX >> 9;
        int endX = (blockX + width) >> 9;
        int startZ = blockZ >> 9;
        int endZ = (blockZ + length) >> 9;

        for (int rx = startX; rx <= endX; ++rx)
        {
            for (int rz = startZ; rz <= endZ; ++rz)
            {
                RegionCoords rc = RegionCoords.fromBlockCoords(rx, rz);
                if (this.imageMap.containsKey(rc) == false)
                {
                    this.imageMap.put(rc, new ImageRegion(rx, rz, Configs.instance.imagePath));
                }
            }
        }
    }
}
