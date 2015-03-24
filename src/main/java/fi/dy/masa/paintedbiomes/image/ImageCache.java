package fi.dy.masa.paintedbiomes.image;

import java.util.HashMap;
import java.util.Map;

import fi.dy.masa.paintedbiomes.util.RegionCoords;

public class ImageCache
{
    public Map<RegionCoords, ImageRegion> imageMap;

    public ImageCache()
    {
        this.imageMap = new HashMap<RegionCoords, ImageRegion>();
    }

    public boolean contains(int blockX, int blockZ)
    {
        return this.imageMap.containsKey(RegionCoords.fromBlockCoords(blockX, blockZ));
    }
}
