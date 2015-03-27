package fi.dy.masa.paintedbiomes.config;

import java.io.File;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.image.ColorToBiomeMapping;

public class Configs
{
    private static Configs instance;
    private Configuration conf;
    private File configFile;

    public int templateAlignmentMode;
    public int templateAlignmentX;
    public int templateAlignmentZ;
    public int templateUndefinedAreaBiome;
    public boolean useSingleTemplateImage;

    public int unpaintedAreaBiome;
    public boolean useCustomColorMappings;

    public Configs(File file)
    {
        instance = this;
        this.configFile = file;
    }

    public static Configs getInstance()
    {
        return instance;
    }

    public void loadConfigs()
    {
        PaintedBiomes.logger.info("Loading configuration...");

        this.conf = new Configuration(this.configFile);
        this.conf.load();
        Property prop;

        String category = "Generic";

        prop = this.conf.get(category, "unpaintedAreaBiomeID", -1);
        prop.comment = "What to do with the areas outside the template image(s). -1 = use the biome from regular world generation, 0..255 = the biome ID to use";
        this.unpaintedAreaBiome = this.checkAndFixValueInt(prop, -1, 255, -1);

        prop = this.conf.get(category, "useCustomColorMappings", false);
        prop.comment = "Whether to use custom assigned colors for biomes. false = blue channel value defines the biome ID (0-255), true = the entire RGB value (excluding alpha!) is used and mapped to the biome.";
        this.useCustomColorMappings = prop.getBoolean();

        category = "TemplateImage";

        prop = this.conf.get(category, "templateAlignmentMode", 0);
        prop.comment = "When using a single template image, how the template image is aligned in the world. The alignment point is defined by templateAlignmentX and templateAlignmentZ. 0 = centered, 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.";
        this.templateAlignmentMode = this.checkAndFixValueInt(prop, 0, 4, 0);

        prop = this.conf.get(category, "templateAlignmentX", 0);
        prop.comment = "The world X coordinate where the selected point (templateAlignmentMode) of the template image is aligned.";
        this.templateAlignmentX = prop.getInt();

        prop = this.conf.get(category, "templateAlignmentZ", 0);
        prop.comment = "The world Z coordinate where the selected point (templateAlignmentMode) of the template image is aligned.";
        this.templateAlignmentZ = prop.getInt();

        prop = this.conf.get(category, "templateUndefinedAreaBiomeID", -1);
        prop.comment = "What to do with the undefined (= completely transparent) areas _inside the template image_. -1 = use the biome from regular world generation, 0..255 = the biome ID to use";
        this.templateUndefinedAreaBiome = this.checkAndFixValueInt(prop, -1, 255, -1);

        prop = this.conf.get(category, "useSingleTemplateImage", false);
        prop.comment = "true = Use only one image template. false = Use multiple image templates for different regions of the world (one image per region file, aka. 512x512 block area).";
        this.useSingleTemplateImage = prop.getBoolean();

        this.readColorToBiomeMappings();

        if (this.conf.hasChanged() == true)
        {
            this.conf.save();
        }
    }

    private void readColorToBiomeMappings()
    {
        ConfigCategory configCategory = this.conf.getCategory("ColorToBiomeMappings");
        configCategory.setComment("Custom mappings from biome name to the RGB color value. Specified as hex values, without the leading \"0x\".");

        ColorToBiomeMapping colorToBiomeMapping = new ColorToBiomeMapping();
        colorToBiomeMapping.setUseCustomMappings(this.useCustomColorMappings);

        // Iterate over the biome array, and for each biome that is found in the config, add the custom color mapping
        BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomes)
        {
            if (biome != null)
            {
                // Mapping found in the config
                if (configCategory.containsKey(biome.biomeName) == true)
                {
                    int val = Integer.parseInt(configCategory.get(biome.biomeName).getString(), 16);
                    // Don't add color mappings as custom mappings if they are just the default mapping from blue channel to BiomeID
                    if (val != biome.biomeID)
                    {
                        colorToBiomeMapping.addCustomMapping(val, biome);
                    }
                }
                // No mapping found, add the default mapping from the BiomeID to blue channel, so that all the existing biomes will get added to the config
                else
                {
                    configCategory.put(biome.biomeName, new Property(biome.biomeName, String.format("%06X", biome.biomeID), Property.Type.STRING));
                }
            }
        }
    }

    public int checkAndFixValueInt(Property prop, int min, int max, int defaultValue)
    {
        int value = prop.getInt();

        if (value < min || value > max)
        {
            value = defaultValue;
            prop.set(value);
        }

        return value;
    }
}
