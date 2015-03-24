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

    public boolean equals(RegionCoords other)
    {
        return this.regionX == other.regionX && this.regionZ == other.regionZ;
    }
}
