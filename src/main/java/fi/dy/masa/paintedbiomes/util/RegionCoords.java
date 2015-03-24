package fi.dy.masa.paintedbiomes.util;

public class RegionCoords
{
    public final int regionX;
    public final int regionZ;

    public RegionCoords(int x, int z)
    {
        this.regionX = x;
        this.regionZ = z;
    }

    public static RegionCoords fromBlockCoords(int x, int z)
    {
        return new RegionCoords(x >> 9, z >> 9);
    }

    /*public boolean equals(RegionCoords other)
    {
        return this.regionX == other.regionX && this.regionZ == other.regionZ;
    }*/

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
        if (getClass() != obj.getClass()) return false;
        RegionCoords other = (RegionCoords) obj;
        if (regionX != other.regionX) return false;
        if (regionZ != other.regionZ) return false;
        return true;
    }
}
