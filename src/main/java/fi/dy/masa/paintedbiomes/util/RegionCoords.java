package fi.dy.masa.paintedbiomes.util;


public class RegionCoords
{
    public static final int REGION_SIZE = 64;
    public final int dimension;
    public final int regionX;
    public final int regionZ;

    public RegionCoords(int dimension, int regionX, int regionZ)
    {
        this.dimension = dimension;
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public static RegionCoords fromBlockCoords(int dimension, int blockX, int blockZ)
    {
        //return new RegionCoords(dimension, blockX >> 9, blockZ >> 9);
        return new RegionCoords(dimension, blockX / REGION_SIZE, blockZ / REGION_SIZE);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + dimension;
        result = prime * result + regionX;
        result = prime * result + regionZ;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RegionCoords other = (RegionCoords) obj;
        if (dimension != other.dimension) return false;
        if (regionX != other.regionX) return false;
        if (regionZ != other.regionZ) return false;
        return true;
    }
}
