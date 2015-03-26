package fi.dy.masa.paintedbiomes.util;

public class RegionCoords
{
    public final int regionX;
    public final int regionZ;

    public RegionCoords(int regionX, int regionZ)
    {
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public static RegionCoords fromBlockCoords(int blockX, int blockZ)
    {
        return new RegionCoords(blockX >> 9, blockZ >> 9);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + regionX;
        result = prime * result + regionZ;

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        RegionCoords other = (RegionCoords) obj;
        if (regionX != other.regionX) return false;
        if (regionZ != other.regionZ) return false;
        return true;
    }
}
