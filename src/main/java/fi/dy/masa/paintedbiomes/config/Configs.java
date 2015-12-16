package fi.dy.masa.paintedbiomes.config;

import java.io.File;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.image.ColorToBiomeMapping;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.reference.Reference;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Configs
{
    private static Configs globalConfigs;
    private static TIntObjectHashMap<Configs> globalPerDimConfigs = new TIntObjectHashMap<Configs>();

    private static Configs worldConfigs;
    private static TIntObjectHashMap<Configs> worldPerDimConfigs = new TIntObjectHashMap<Configs>();

    private static File globalConfigDir;
    private static File globalConfigFile;
    private static File worldConfigDir;
    private static File worldConfigFile;

    private File configFile;
    private boolean isMaster;

    public boolean useGenLayer;
    public int[] enabledInDimensions;
    public boolean overrideChunkProvider;
    public String chunkProviderType;
    public String chunkProviderOptions;

    public int templateAlignmentMode;
    public int templateAlignmentX;
    public int templateAlignmentZ;
    public int templateUndefinedAreaBiome;
    public int unpaintedAreaBiome;
    public boolean useSingleTemplateImage;
    private boolean useCustomColorMappings;

    public boolean useTemplateRepeating;
    public int repeatTemplatePositiveX;
    public int repeatTemplatePositiveZ;
    public int repeatTemplateNegativeX;
    public int repeatTemplateNegativeZ;

    private Configs(File configFile, boolean isMaster)
    {
        this.configFile = configFile;
        this.isMaster = isMaster;
        this.enabledInDimensions = new int[0];
        this.templateUndefinedAreaBiome = -1;
        this.unpaintedAreaBiome = -1;
        this.useSingleTemplateImage = true;
        this.useCustomColorMappings = true;
        this.chunkProviderType = "";
        this.chunkProviderOptions = "";
    }

    private Configs(File configDir, int dimension)
    {
        this(getConfigFileForDimension(configDir, dimension), false);
    }

    private static File getConfigFileForDimension(File configDir, int dimension)
    {
        return new File(configDir, Reference.MOD_ID + "_dim" + dimension + ".cfg");
    }

    public static Configs getEffectiveMainConfig()
    {
        return worldConfigs != null ? worldConfigs : globalConfigs;
    }

    /**
     * Initialize the configuration directory and file locations. Call once for example from preInit.
     */
    public static void setConfigDir(File modConfigDir)
    {
        globalConfigDir = new File(modConfigDir, Reference.MOD_ID);
        globalConfigFile = new File(globalConfigDir, Reference.MOD_ID + ".cfg");
        globalConfigs = new Configs(globalConfigFile, true).loadConfigs();
    }

    private static void setWorldDir()
    {
        File worldDir = DimensionManager.getCurrentSaveRootDirectory();
        if (worldDir != null)
        {
            worldConfigDir = new File(worldDir, Reference.MOD_ID);
            worldConfigFile = new File(worldConfigDir, Reference.MOD_ID + ".cfg");
        }
    }

    /**
     * Re-create the configuration instances and re-read all the configuration files.
     * Can only be called after calling init() once.
     */
    public static void reload()
    {
        setWorldDir();

        globalConfigs = new Configs(globalConfigFile, true).loadConfigs();
        worldConfigs = (worldConfigFile != null && worldConfigFile.exists() == true && worldConfigFile.isFile() == true) ? new Configs(worldConfigFile, true).copyFrom(globalConfigs).loadConfigs() : null;

        globalPerDimConfigs.clear();
        worldPerDimConfigs.clear();

        loadPerDimensionConfigs();

        ImageHandler.setTemplateBasePaths(new File(globalConfigDir, "templates"), new File(worldConfigDir, "templates"));
    }

    private static void loadPerDimensionConfigs()
    {
        Configs mainConfig = globalConfigs;
        for (int dimension : mainConfig.enabledInDimensions)
        {
            Configs conf = globalPerDimConfigs.get(dimension);
            if (conf == null)
            {
                File file = getConfigFileForDimension(globalConfigDir, dimension);
                if (file.exists() == true && file.isFile() == true)
                {
                    globalPerDimConfigs.put(dimension, new Configs(globalConfigDir, dimension).copyFrom(mainConfig).loadConfigs());
                }
            }
        }

        mainConfig = getEffectiveMainConfig();
        for (int dimension : mainConfig.enabledInDimensions)
        {
            Configs conf = worldPerDimConfigs.get(dimension);
            if (conf == null)
            {
                File file = getConfigFileForDimension(worldConfigDir, dimension);
                if (file.exists() == true && file.isFile() == true)
                {
                    worldPerDimConfigs.put(dimension, new Configs(worldConfigDir, dimension).copyFrom(mainConfig).loadConfigs());
                }
            }
        }
    }

    public static Configs getConfig(int dimension)
    {
        Configs conf = worldPerDimConfigs.get(dimension);
        if (conf != null)
        {
            return conf;
        }

        conf = globalPerDimConfigs.get(dimension);
        return conf != null ? conf : getEffectiveMainConfig();
    }

    private Configs copyFrom(Configs old)
    {
        this.templateAlignmentMode      = old.templateAlignmentMode;
        this.templateAlignmentX         = old.templateAlignmentX;
        this.templateAlignmentZ         = old.templateAlignmentZ;
        this.templateUndefinedAreaBiome = old.templateUndefinedAreaBiome;
        this.unpaintedAreaBiome         = old.unpaintedAreaBiome;
        this.useSingleTemplateImage     = old.useSingleTemplateImage;
        this.useTemplateRepeating       = old.useTemplateRepeating;
        this.repeatTemplatePositiveX    = old.repeatTemplatePositiveX;
        this.repeatTemplatePositiveZ    = old.repeatTemplatePositiveZ;
        this.repeatTemplateNegativeX    = old.repeatTemplateNegativeX;
        this.repeatTemplateNegativeZ    = old.repeatTemplateNegativeZ;
        this.overrideChunkProvider      = old.overrideChunkProvider;
        this.chunkProviderType          = old.chunkProviderType;
        this.chunkProviderOptions       = old.chunkProviderOptions;
        this.enabledInDimensions        = old.enabledInDimensions.clone();

        return this;
    }

    private Configs loadConfigs()
    {
        PaintedBiomes.logger.info("Loading configuration from '" + this.configFile.getAbsolutePath() + "'");

        Configuration conf = new Configuration(this.configFile, null, true); // true: Use case sensitive category names
        conf.load();
        Property prop;

        String category = "TemplateImage";

        prop = conf.get(category, "templateAlignmentMode", this.templateAlignmentMode);
        prop.comment = "When using a single template image, how the template image is aligned in the world. The alignment point is defined by templateAlignmentX and templateAlignmentZ. 0 = centered, 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.";
        this.templateAlignmentMode = this.checkAndFixConfigValueInt("templateAlignmentMode", prop, 0, 4, 0);

        prop = conf.get(category, "templateAlignmentX", this.templateAlignmentX);
        prop.comment = "The world X coordinate where the selected point (templateAlignmentMode) of the template image is aligned.";
        this.templateAlignmentX = prop.getInt();

        prop = conf.get(category, "templateAlignmentZ", this.templateAlignmentZ);
        prop.comment = "The world Z coordinate where the selected point (templateAlignmentMode) of the template image is aligned.";
        this.templateAlignmentZ = prop.getInt();

        prop = conf.get(category, "templateUndefinedAreaBiomeID", this.templateUndefinedAreaBiome);
        prop.comment = "How to handle \"undefined\" (= completely transparent) areas within the template image area(s). -1 = Use the biome from regular terrain generation, 0..255 = the biome ID to use.";
        this.templateUndefinedAreaBiome = this.checkAndFixBiomeID("templateUndefinedAreaBiomeID", prop, -1);

        prop = conf.get(category, "unpaintedAreaBiomeID", this.unpaintedAreaBiome);
        prop.comment = "Biome handling outside of the template image(s). -1 = Use the biome from regular terrain generation, 0..255 = the Biome ID to use.";
        this.unpaintedAreaBiome = this.checkAndFixBiomeID("unpaintedAreaBiomeID", prop, -1);

        prop = conf.get(category, "useSingleTemplateImage", this.useSingleTemplateImage);
        prop.comment = "true = Use only one image template (biomes.png). false = Use multiple image templates for different regions of the world (one image per region file, aka. 512x512 block area).";
        this.useSingleTemplateImage = prop.getBoolean();

        category = "TemplateRepeating";

        ConfigCategory cat = conf.getCategory(category);
        cat.setComment("Template repeating options. Template repeating only works in the Single Template Image mode.");

        prop = conf.get(category, "repeatTemplateNegativeX", this.repeatTemplateNegativeX);
        prop.comment = "Repeat the template image in the negative X direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplateNegativeX = this.checkAndFixConfigValueInt("repeatTemplateNegativeX", prop, 0, 2, 0);

        prop = conf.get(category, "repeatTemplateNegativeZ", this.repeatTemplateNegativeZ);
        prop.comment = "Repeat the template image in the negative Z direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplateNegativeZ = this.checkAndFixConfigValueInt("repeatTemplateNegativeZ", prop, 0, 2, 0);

        prop = conf.get(category, "repeatTemplatePositiveX", this.repeatTemplatePositiveX);
        prop.comment = "Repeat the template image in the positive X direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplatePositiveX = this.checkAndFixConfigValueInt("repeatTemplatePositiveX", prop, 0, 2, 0);

        prop = conf.get(category, "repeatTemplatePositiveZ", this.repeatTemplatePositiveZ);
        prop.comment = "Repeat the template image in the positive Z direction. 0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image";
        this.repeatTemplatePositiveZ = this.checkAndFixConfigValueInt("repeatTemplatePositiveZ", prop, 0, 2, 0);

        prop = conf.get(category, "useTemplateRepeating", this.useTemplateRepeating);
        prop.comment = "Enable template repeating. Note that you have to also select the directions that you want to repeat in and the repeating method.";
        this.useTemplateRepeating = prop.getBoolean();

        category = "Generic";

        prop = conf.get(category, "overrideChunkProvider", this.overrideChunkProvider);
        prop.comment = "Set to true to use an overridden ChunkProvider. Select the type in chunkProviderType.";
        this.overrideChunkProvider = prop.getBoolean();

        prop = conf.get(category, "chunkProviderType", this.chunkProviderType);
        prop.comment = "The ChunkProvider to use. Valid values: VANILLA_DEFAULT, VANILLA_FLAT, VANILLA_HELL, VANILLA_END";
        this.chunkProviderType = prop.getString() != null ? prop.getString() : "";

        prop = conf.get(category, "chunkProviderOptions", this.chunkProviderOptions);
        prop.comment = "Extra options for the ChunkProvider (mostly for VANILLA_FLAT).";
        this.chunkProviderOptions = prop.getString() != null ? prop.getString() : "";

        // These config values only exist and are used in the non-per-dimension configs
        if (this.isMaster == true)
        {
            prop = conf.get(category, "enabledInDimensions", this.enabledInDimensions);
            prop.comment = "A list of dimensions where Painted Biomes should be enabled.";
            this.enabledInDimensions = prop.getIntList();

            prop = conf.get(category, "useCustomColorsAsDefaults", this.useCustomColorMappings);
            prop.comment = "This only affects whether the missing ColorToBiomeMappings values, when added, use the custom colors from Amidst, or if they just map the Biome ID to the red channel. true = Use custom colors from Amidst as defaults, false = Map biome ID to red channel.";
            this.useCustomColorMappings = prop.getBoolean();

            prop = conf.get(category, "useGenLayer", this.useGenLayer);
            prop.comment = "Use biome GenLayer overrides instead of a WorldChunkManager wrapper. This only works in the Overworld, and not with many custom WorldTypes. This is the method used until and including the v0.3.0 release.";
            this.useGenLayer = prop.getBoolean();

            this.readColorToBiomeMappings(conf);
        }

        if (conf.hasChanged() == true)
        {
            conf.save();
        }

        return this;
    }

    private void readColorToBiomeMappings(Configuration conf)
    {
        ConfigCategory configCategory = conf.getCategory("ColorToBiomeMappings");
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
