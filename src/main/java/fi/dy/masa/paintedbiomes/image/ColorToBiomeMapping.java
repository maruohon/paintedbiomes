package fi.dy.masa.paintedbiomes.image;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;

public class ColorToBiomeMapping
{
    private static ColorToBiomeMapping instance;
    /** Mapping from an RGB color value to a Biome ID */
    public Map<Integer, BiomeGenBase> customMappings;
    public boolean useCustomMappings;

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
        customMappings = new HashMap<Integer, BiomeGenBase>();
    }

    public void addCustomMapping(int color, BiomeGenBase biome)
    {
        this.customMappings.put(Integer.valueOf(color), biome);
    }

    public BiomeGenBase getBiomeForColor(int color)
    {
        if (this.useCustomMappings == true)
        {
            return this.customMappings.get(Integer.valueOf(color & 0x00FFFFFF));
        }

        // Default mapping: use the value of the blue channel as the BiomeID
        return BiomeGenBase.getBiome(color & 0x000000FF);
    }
}
