package fi.dy.masa.paintedbiomes.image;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ColorToBiomeMapping
{
    private static ColorToBiomeMapping instance;
    /** Mapping from an RGB color value to a Biome ID */
    private TIntObjectHashMap<Integer> customMappings;
    private boolean useCustomMappings;

    public ColorToBiomeMapping()
    {
        instance = this;
        this.clearCustomMappings();
    }

    public static ColorToBiomeMapping getInstance()
    {
        return instance;
    }

    public void setUseCustomMappings(boolean value)
    {
        this.useCustomMappings = value;
    }

    public void clearCustomMappings()
    {
        this.customMappings = new TIntObjectHashMap<Integer>();
    }

    public void addCustomMapping(int color, int biomeID)
    {
        this.customMappings.put(color & 0x00FFFFFF, biomeID);
    }

    public int getBiomeIDForColor(int color)
    {
        if (this.useCustomMappings == true)
        {
            Integer biomeID = this.customMappings.get(color & 0x00FFFFFF);

            return (biomeID != null ? biomeID.intValue() : -1);
        }

        // Default mapping: use the value of the blue channel as the BiomeID
        return color & 0x000000FF;
    }
}
