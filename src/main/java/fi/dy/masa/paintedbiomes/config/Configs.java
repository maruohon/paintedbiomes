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

    public boolean useGenLayer;
    public int[] enabledInDimensions;

    public int templateAlignmentMode;
    public int templateAlignmentX;
    public int templateAlignmentZ;
    public int templateUndefinedAreaBiome;
    public boolean useSingleTemplateImage;

    public int unpaintedAreaBiome;
    private boolean useCustomColorMappings;

    public boolean useTemplateRepeating;
    public int repeatTemplatePositiveX;
    public int repeatTemplatePositiveZ;
    public int repeatTemplateNegativeX;
    public int repeatTemplateNegativeZ;

    public Configs(File file)
    {
        this.configFile = file;
    }

    public static void init(File file)
    {
        instance = new Configs(file);
    }

    public static Configs getInstance()
    {
        return instance;
    }

    public void loadConfigs()
    {
        PaintedBiomes.logger.info("Loading configuration...");

        this.conf = new Configuration(this.configFile, null, true); // true: Use case sensitive category names
        this.conf.load();
        Property prop;

        String category = "Generic";

        prop = this.conf.get(category, "useGenLayer", false);
        prop.comment = "Use biome GenLayer overrides instead of a WorldChunkManager wrapper. This only works in the Overworld, and not with many custom WorldTypes. This is the method used up until the v0.3.0 release.";
        this.useGenLayer = prop.getBoolean();

        prop = this.conf.get(category, "enabledInDimensions", new int[0]);
        prop.comment = "A list of dimensions where Painted Biomes should be enabled.";
        this.enabledInDimensions = prop.getIntList();

        prop = this.conf.get(category, "unpaintedAreaBiomeID", -1);
        prop.comment = "Biome handling outside of the template image(s). -1 = Use the biome from regular terrain generation, 0..255 = the Biome ID to use.";
        this.unpaintedAreaBiome = this.checkAndFixBiomeID("unpaintedAreaBiomeID", prop, -1);

        prop = this.conf.get(category, "useCustomColorsAsDefaults", true);
        prop.comment = "This only affects whether the missing ColorToBiomeMappings values, when added, use the custom colors from Amidst, or if they just map the Biome ID to the red channel. true = Use custom colors from Amidst as defaults, false = Map biome ID to red channel.";
        this.useCustomColorMappings = prop.getBoolean();

        category = "TemplateImage";

        prop = this.conf.get(category, "templateAlignmentMode", 0);
        prop.comment = "When using a single template image, how the template image is aligned in the world. The alignment point is defined by templateAlignmentX and templateAlignmentZ. 0 = centered, 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.";
        this.templateAlignmentMode = this.checkAndFixConfigValueInt("templateAlignmentMode", prop, 0, 4, 0);

        prop = this.conf.get(category, "templateAlignmentX", 0);
        prop.comment = "The world X coordinate where the selected point (templateAlignmentMode) of the template image is aligned.";
        this.templateAlignmentX = prop.getInt();

        prop = this.conf.get(category, "templateAlignmentZ", 0);
        prop.comment = "The world Z coordinate where the selected point (templateAlignmentMode) of the template image is aligned.";
        this.templateAlignmentZ = prop.getInt();

        prop = this.conf.get(category, "templateUndefinedAreaBiomeID", -1);
        prop.comment = "How to handle \"undefined\" (= completely transparent) areas within the template image area(s). -1 = Use the biome from regular terrain generation, 0..255 = the biome ID to use.";
        this.templateUndefinedAreaBiome = this.checkAndFixBiomeID("templateUndefinedAreaBiomeID", prop, -1);

        prop = this.conf.get(category, "useSingleTemplateImage", true);
        prop.comment = "true = Use only one image template (biomes.png). false = Use multiple image templates for different regions of the world (one image per region file, aka. 512x512 block area).";
        this.useSingleTemplateImage = prop.getBoolean();

        category = "TemplateRepeating";

        ConfigCategory cat = this.conf.getCategory(category);
        cat.setComment("Template repeating options. Template repeating only works in the Single Template mode.");

        prop = this.conf.get(category, "useTemplateRepeating", false);
        prop.comment = "Enable template repeating. Note that you have to also select the directions that you want to repeat in and the repeating method.";
        this.useTemplateRepeating = prop.getBoolean();

        prop = this.conf.get(category, "repeatTemplatePositiveX", 0);
        prop.comment = "Repeat the template image in the positive X direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplatePositiveX = this.checkAndFixConfigValueInt("repeatTemplatePositiveX", prop, 0, 2, 0);

        prop = this.conf.get(category, "repeatTemplatePositiveZ", 0);
        prop.comment = "Repeat the template image in the positive Z direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplatePositiveZ = this.checkAndFixConfigValueInt("repeatTemplatePositiveZ", prop, 0, 2, 0);

        prop = this.conf.get(category, "repeatTemplateNegativeX", 0);
        prop.comment = "Repeat the template image in the negative X direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplateNegativeX = this.checkAndFixConfigValueInt("repeatTemplateNegativeX", prop, 0, 2, 0);

        prop = this.conf.get(category, "repeatTemplateNegativeZ", 0);
        prop.comment = "Repeat the template image in the negative Z direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplateNegativeZ = this.checkAndFixConfigValueInt("repeatTemplateNegativeZ", prop, 0, 2, 0);

        this.readColorToBiomeMappings();

        if (this.conf.hasChanged() == true)
        {
            this.conf.save();
        }
    }

    private void readColorToBiomeMappings()
    {
        ConfigCategory configCategory = this.conf.getCategory("ColorToBiomeMappings");
        configCategory.setComment("Mappings from biome name to the RGB color value. These are now always used! Specified as hex values, without the leading '0x'.");

        ColorToBiomeMapping colorToBiomeMapping = new ColorToBiomeMapping();

        // Iterate over the biome array and add a color-to-biome mapping for all of them
        BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomes)
        {
            if (biome == null)
            {
                continue;
            }

            Property prop;
            // Default mapping is from the Biome ID to the red channel
            int color = (biome.biomeID & 0xFF) << 16;

            // Mapping found in the config, use that color value
            if (configCategory.containsKey(biome.biomeName) == true)
            {
                prop = configCategory.get(biome.biomeName);

                try
                {
                    color = Integer.parseInt(prop.getString(), 16);
                }
                catch (NumberFormatException e)
                {
                    PaintedBiomes.logger.warn(String.format("Failed to parse color value '%s' for biome '%s'", prop.getString(), biome.biomeName));
                }
            }
            // No mapping found in the config, add a default mapping, so that all the existing biomes will get added to the config
            else
            {
                if (this.useCustomColorMappings == true)
                {
                    // Try to get the default custom color, if one is defined for this biome
                    Integer colorInteger = DefaultColorMappings.getColorForBiome(biome.biomeName);
                    if (colorInteger != null)
                    {
                        color = colorInteger.intValue();
                    }
                }

                prop = new Property(biome.biomeName, String.format("%06X", color), Property.Type.STRING);
                configCategory.put(biome.biomeName, prop);
            }

            // For simplicity, when generating terrain, the biome is always read from the mapping, even in case of a red channel mapping.
            // So basically we want to always add all the existing biomes to the color-to-biome map.
            colorToBiomeMapping.addMapping(color, biome.biomeID);

            // Update the comment, in case the biome ID has been changed since the config was first generated
            prop.comment = "Biome name: " + biome.biomeName + ", Biome ID: " + biome.biomeID + " (Color as int: " + color + ")";
        }
    }

    private int checkAndFixConfigValueInt(String configName, Property prop, int min, int max, int defaultValue)
    {
        int value = prop.getInt();

        if (value < min || value > max)
        {
            PaintedBiomes.logger.warn(String.format("Invalid config value for %s: '%d', setting it to '%d'.", configName, value, defaultValue));
            value = defaultValue;
            prop.set(value);
        }

        return value;
    }

    private int checkAndFixBiomeID(String configName, Property prop, int defaultBiomeId)
    {
        int biomeId = this.checkAndFixConfigValueInt(configName, prop, -1, BiomeGenBase.getBiomeGenArray().length - 1, defaultBiomeId);

        if (biomeId >= 0 && BiomeGenBase.getBiomeGenArray()[biomeId] == null)
        {
            PaintedBiomes.logger.warn(String.format("Invalid/non-existing Biome ID '%d' for config %s, setting the value to '%d'.", biomeId, configName, defaultBiomeId));
            biomeId = defaultBiomeId;
            prop.set(biomeId);
        }

        return biomeId;
    }
}
