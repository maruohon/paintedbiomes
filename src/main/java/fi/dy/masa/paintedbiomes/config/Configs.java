package fi.dy.masa.paintedbiomes.config;

import java.io.File;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import fi.dy.masa.paintedbiomes.image.ColorToBiomeMapping;

public class Configs
{
    public static Configs instance;
    private Configuration conf;
    private File configFile;
    public boolean useCustomMappings;

    public Configs(File file)
    {
        instance = this;
        this.configFile = file;
    }

    public void loadConfigs()
    {
        this.conf = new Configuration(this.configFile);
        Property prop;
        String category = "Generic";

        prop = this.conf.get(category, "useCustomMappings", false);
        prop.comment = "Whether to use custom assigned colors for biomes. If false, then the blue value defines the biome ID (0-255). Transparent pixels (and regions with missing images) will use the biome from the underlying WorldChunkManager.";
        this.useCustomMappings = prop.getBoolean();

        category = "CustomMappings";
        ConfigCategory cat = this.conf.getCategory(category);
        cat.setComment("Custom mappings from biome name to the ARGB color value. Specified as hex values, without the leading \"0x\".");

        ColorToBiomeMapping ctb = new ColorToBiomeMapping();
        ctb.setUseCustomMappings(this.useCustomMappings);

        // Iterate over the biome array, and for each biome that is found in the config, add the custom color mapping
        BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomes)
        {
            if (biome != null)
            {
                // Mapping found in the config
                if (cat.containsKey(biome.biomeName) == true)
                {
                    int val = Integer.parseInt(cat.get(biome.biomeName).getString(), 16);
                    // Don't add color mappings as custom mappings if they are just the default blue channel mapping
                    if (val != biome.biomeID)
                    {
                        ctb.addCustomMapping(val, biome);
                    }
                }
                // No mapping found, add the default mapping to the blue channel, so that all the existing biomes will get added to the config
                else
                {
                    cat.put(biome.biomeName, new Property(biome.biomeName, String.format("%06X", biome.biomeID), Property.Type.STRING));
                }
            }
        }

        if (this.conf.hasChanged() == true)
        {
            this.conf.save();
        }
    }
}
