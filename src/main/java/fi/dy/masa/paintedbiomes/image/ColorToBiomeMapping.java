package fi.dy.masa.paintedbiomes.image;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ColorToBiomeMapping
{
    private static ColorToBiomeMapping instance;
    /** Mapping from an RGB color value to a Biome ID */
    private TIntObjectHashMap<Integer> colorToBiomeMappings;

    public ColorToBiomeMapping()
    {
        instance = this;
        this.initMappings();
    }

    public static ColorToBiomeMapping getInstance()
    {
        return instance;
    }

    public void initMappings()
    {
        this.colorToBiomeMappings = new TIntObjectHashMap<Integer>();
    }

    public void addMapping(int color, int biomeID)
    {
        this.colorToBiomeMappings.put(color & 0x00FFFFFF, biomeID);
    }

    /** Returns the Biome ID to use for the given color. If there is no mapping for the given color, then -1 is returned.
     * @param color
     * @return The Biome ID to use, or -1 to indicate that we should use the Biome from the regular terrain generation
     */
    public int getBiomeIDForColor(int color)
    {
        Integer biomeInteger = this.colorToBiomeMappings.get(color & 0x00FFFFFF);

        // Default for unknown colors is to use the biome from the regular terrain generation
        return biomeInteger != null ? biomeInteger.intValue() : -1;
    }
}
