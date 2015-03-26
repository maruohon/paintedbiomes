package fi.dy.masa.paintedbiomes.image;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;

public class ColorToBiomeMapping
{
    public static ColorToBiomeMapping instance;
    /** Mapping from an RGB color value to a Biome ID */
    public Map<Integer, BiomeGenBase> customMappings;
    public boolean useCustomMappings;

    public ColorToBiomeMapping()
    {
        instance = this;
        this.clearCustomMappings();
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
            Integer colorInt = Integer.valueOf(color & 0x00FFFFFF);
            if (this.customMappings.containsKey(colorInt) == true)
            {
                return this.customMappings.get(colorInt);
            }
            // TODO: return a default here, like Ocean
        }

        // Default mapping: use the value of the blue channel as the Biome ID
        return BiomeGenBase.getBiome(color & 0x000000FF);
    }
}
