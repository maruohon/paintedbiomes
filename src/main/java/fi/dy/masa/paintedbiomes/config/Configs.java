package fi.dy.masa.paintedbiomes.config;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.image.ColorToBiomeMapping;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.reference.Reference;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Configs
{
    private static Configs globalConfigs;
    private static Configs worldConfigs;
    private static final TIntObjectHashMap<Configs> PER_DIM_CONFIGS_GLOBAL = new TIntObjectHashMap<Configs>();
    private static final TIntObjectHashMap<Configs> PER_DIM_CONFIGS_WORLD = new TIntObjectHashMap<Configs>();

    private static File globalConfigDir;
    private static File globalConfigFile;
    private static File worldConfigDir;
    private static File worldConfigFile;

    private File configFile;
    private boolean isMaster;
    private boolean useBGROrderInConfig;
    private boolean useCustomColorMappings;
    public boolean useGenLayer;
    public int[] enabledInDimensions;

    public boolean overrideChunkProvider;
    public String chunkProviderType = "";
    public String chunkProviderOptions = "";

    public int templateAlignmentMode;
    public int templateAlignmentX;
    public int templateAlignmentZ;
    public boolean templateAlignToWorldSpawn;

    public String templateUndefinedAreaBiomeName = "";
    public String unpaintedAreaBiomeName = "";
    public boolean useSingleTemplateImage;

    public boolean useTemplateRandomRotation;
    public boolean useTemplateRandomFlipping;
    public boolean useAlternateTemplates;
    public int maxAlternateTemplates;

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
        this.useSingleTemplateImage = true;
        this.useCustomColorMappings = true;
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
        globalConfigs = new Configs(globalConfigFile, true);
    }

    private static void setWorldDir()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        worldConfigDir = null;
        worldConfigFile = null;

        if (server != null && server.getFolderName() != null)
        {
            ISaveFormat saveConverter = server.getActiveAnvilConverter();

            if (saveConverter != null && (saveConverter instanceof SaveFormatOld))
            {
                File savesDirectory = ((SaveFormatOld) saveConverter).savesDirectory;
                File worldDir = new File(savesDirectory, server.getFolderName());
                worldConfigDir = new File(worldDir, Reference.MOD_ID);
                worldConfigFile = new File(worldConfigDir, Reference.MOD_ID + ".cfg");
            }
        }
    }

    /**
     * Re-create the configuration instances and re-read all the configuration files.
     * Can only be called after calling setConfigDir() once.
     */
    public static void reload()
    {
        setWorldDir();

        globalConfigs = new Configs(globalConfigFile, true).loadConfigs();
        worldConfigs = null;

        if (worldConfigFile != null && worldConfigFile.exists() && worldConfigFile.isFile())
        {
            worldConfigs = new Configs(worldConfigFile, true).copyFrom(globalConfigs).loadConfigs();
        }

        loadPerDimensionConfigs();

        File worldTemplateDir = (worldConfigDir != null) ? new File(worldConfigDir, "templates") : null;
        ImageHandler.setTemplateBasePaths(new File(globalConfigDir, "templates"), worldTemplateDir);
    }

    private static void loadPerDimensionConfigs()
    {
        PER_DIM_CONFIGS_GLOBAL.clear();

        Configs mainConfig = globalConfigs;

        for (int dimension : mainConfig.enabledInDimensions)
        {
            Configs conf = PER_DIM_CONFIGS_GLOBAL.get(dimension);

            if (conf == null)
            {
                File file = getConfigFileForDimension(globalConfigDir, dimension);

                if (file.exists() && file.isFile())
                {
                    PER_DIM_CONFIGS_GLOBAL.put(dimension, new Configs(globalConfigDir, dimension).copyFrom(mainConfig).loadConfigs());
                }
            }
        }

        PER_DIM_CONFIGS_WORLD.clear();

        if (worldConfigDir == null)
        {
            return;
        }

        mainConfig = getEffectiveMainConfig();

        for (int dimension : mainConfig.enabledInDimensions)
        {
            Configs conf = PER_DIM_CONFIGS_WORLD.get(dimension);

            if (conf == null)
            {
                File file = getConfigFileForDimension(worldConfigDir, dimension);

                if (file.exists() && file.isFile())
                {
                    PER_DIM_CONFIGS_WORLD.put(dimension, new Configs(worldConfigDir, dimension).copyFrom(mainConfig).loadConfigs());
                }
            }
        }
    }

    public static Configs getConfig(int dimension)
    {
        Configs conf = PER_DIM_CONFIGS_WORLD.get(dimension);

        if (conf != null)
        {
            return conf;
        }

        conf = PER_DIM_CONFIGS_GLOBAL.get(dimension);
        return conf != null ? conf : getEffectiveMainConfig();
    }

    private Configs copyFrom(Configs old)
    {
        this.enabledInDimensions = Arrays.copyOf(old.enabledInDimensions, old.enabledInDimensions.length);

        this.overrideChunkProvider      = old.overrideChunkProvider;
        this.chunkProviderType          = old.chunkProviderType;
        this.chunkProviderOptions       = old.chunkProviderOptions;

        this.templateAlignmentMode      = old.templateAlignmentMode;
        this.templateAlignmentX         = old.templateAlignmentX;
        this.templateAlignmentZ         = old.templateAlignmentZ;
        this.templateAlignToWorldSpawn  = old.templateAlignToWorldSpawn;

        this.templateUndefinedAreaBiomeName = old.templateUndefinedAreaBiomeName;
        this.unpaintedAreaBiomeName         = old.unpaintedAreaBiomeName;
        this.useSingleTemplateImage     = old.useSingleTemplateImage;

        this.useTemplateRandomRotation  = old.useTemplateRandomRotation;
        this.useTemplateRandomFlipping  = old.useTemplateRandomFlipping;
        this.useAlternateTemplates      = old.useAlternateTemplates;
        this.maxAlternateTemplates      = old.maxAlternateTemplates;

        this.useTemplateRepeating       = old.useTemplateRepeating;
        this.repeatTemplatePositiveX    = old.repeatTemplatePositiveX;
        this.repeatTemplatePositiveZ    = old.repeatTemplatePositiveZ;
        this.repeatTemplateNegativeX    = old.repeatTemplateNegativeX;
        this.repeatTemplateNegativeZ    = old.repeatTemplateNegativeZ;

        return this;
    }

    private Configs loadConfigs()
    {
        PaintedBiomes.logger.info("Loading configuration from '{}'", this.configFile.getAbsolutePath());

        Configuration conf = new Configuration(this.configFile, null, true); // true: Use case sensitive category names
        conf.load();
        Property prop;

        String category = "TemplateImage";

        prop = conf.get(category, "maxAlternateTemplates", this.maxAlternateTemplates);
        prop.setComment("The maximum number of alternate templates to use.\n" +
                        "NOTE: Especially with large images, the memory requirements can increase significantly!!");
        this.maxAlternateTemplates = this.checkAndFixConfigValueInt("maxAlternateTemplates", prop, 0, 10, 0);

        prop = conf.get(category, "templateAlignmentMode", this.templateAlignmentMode);
        prop.setComment("When using a single template image, how the template image is aligned in the world.\n" +
                        "The alignment point is defined by templateAlignmentX and templateAlignmentZ.\n" +
                        "0 = centered, 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.");
        this.templateAlignmentMode = this.checkAndFixConfigValueInt("templateAlignmentMode", prop, 0, 4, 0);

        prop = conf.get(category, "templateAlignmentX", this.templateAlignmentX);
        prop.setComment("The world X coordinate where the selected point (templateAlignmentMode) of the template image is aligned.");
        this.templateAlignmentX = prop.getInt();

        prop = conf.get(category, "templateAlignmentZ", this.templateAlignmentZ);
        prop.setComment("The world Z coordinate where the selected point (templateAlignmentMode) of the template image is aligned.");
        this.templateAlignmentZ = prop.getInt();

        prop = conf.get(category, "templateAlignToWorldSpawn", this.templateAlignToWorldSpawn);
        prop.setComment("Should the template be aligned to the world spawn point,\ninstead of the templateAlignmentX and templateAlignmentZ coordinates");
        this.templateAlignToWorldSpawn = prop.getBoolean();

        prop = conf.get(category, "templateUndefinedAreaBiome", this.templateUndefinedAreaBiomeName);
        prop.setComment("How to handle \"undefined\" (= completely transparent) areas within the template image area(s).\n" +
                        "<empty or invalid biome registry name> = Use the biome from regular terrain generation\n" +
                        "<a valid biome registry name> = the biome to use");
        this.templateUndefinedAreaBiomeName = prop.getString();

        prop = conf.get(category, "unpaintedAreaBiome", this.unpaintedAreaBiomeName);
        prop.setComment("Biome handling outside of the template image(s).\n" +
                        "<empty or invalid biome registry name> = Use the biome from regular terrain generation\n" +
                        "<a valid biome registry name> = the biome to use");
        this.unpaintedAreaBiomeName = prop.getString();

        prop = conf.get(category, "useAlternateTemplates", this.useAlternateTemplates);
        prop.setComment("Enable using randomly selected alternate templates (based on the world seed and the relative location).");
        this.useAlternateTemplates = prop.getBoolean();

        prop = conf.get(category, "useSingleTemplateImage", this.useSingleTemplateImage);
        prop.setComment("true = Use only one image template (biomes.png).\n" +
                        "false = Use multiple image templates for different regions of the world (one image per region file, ie. a 512x512 block area).");
        this.useSingleTemplateImage = prop.getBoolean();

        prop = conf.get(category, "useTemplateRandomFlipping", this.useTemplateRandomFlipping);
        prop.setComment("Enable random flipping/mirroring of the template images (based on the world seed and the relative location).");
        this.useTemplateRandomFlipping = prop.getBoolean();

        prop = conf.get(category, "useTemplateRandomRotation", this.useTemplateRandomRotation);
        prop.setComment("Enable random rotation of the template images (based on the world seed and the relative location).");
        this.useTemplateRandomRotation = prop.getBoolean();

        category = "TemplateRepeating";

        ConfigCategory cat = conf.getCategory(category);
        cat.setComment("Template repeating options. Template repeating only works in the Single Template Image mode.");

        prop = conf.get(category, "repeatTemplateNegativeX", this.repeatTemplateNegativeX);
        prop.setComment("Repeat the template image in the negative X direction.\n" +
                        "0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image");
        this.repeatTemplateNegativeX = this.checkAndFixConfigValueInt("repeatTemplateNegativeX", prop, 0, 2, 0);

        prop = conf.get(category, "repeatTemplateNegativeZ", this.repeatTemplateNegativeZ);
        prop.setComment("Repeat the template image in the negative Z direction.\n" +
                        "0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image");
        this.repeatTemplateNegativeZ = this.checkAndFixConfigValueInt("repeatTemplateNegativeZ", prop, 0, 2, 0);

        prop = conf.get(category, "repeatTemplatePositiveX", this.repeatTemplatePositiveX);
        prop.setComment("Repeat the template image in the positive X direction.\n" +
                        "0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image");
        this.repeatTemplatePositiveX = this.checkAndFixConfigValueInt("repeatTemplatePositiveX", prop, 0, 2, 0);

        prop = conf.get(category, "repeatTemplatePositiveZ", this.repeatTemplatePositiveZ);
        prop.setComment("Repeat the template image in the positive Z direction.\n" +
                        "0 = disabled, 1 = repeat the entire template, 2 = repeat/continue the biome of the edge-most pixel of the template image");
        this.repeatTemplatePositiveZ = this.checkAndFixConfigValueInt("repeatTemplatePositiveZ", prop, 0, 2, 0);

        prop = conf.get(category, "useTemplateRepeating", this.useTemplateRepeating);
        prop.setComment("Enable template repeating. Note that you have to also select the directions\n" +
                        "that you want to repeat in and the repeating method.");
        this.useTemplateRepeating = prop.getBoolean();

        category = "Generic";

        prop = conf.get(category, "overrideChunkProvider", this.overrideChunkProvider);
        prop.setComment("Set to true to use an overridden ChunkProvider. Select the type in chunkProviderType.");
        this.overrideChunkProvider = prop.getBoolean();

        prop = conf.get(category, "chunkProviderType", this.chunkProviderType);
        prop.setComment("The ChunkProvider to use. Valid values: VANILLA_DEFAULT, VANILLA_FLAT, VANILLA_HELL, VANILLA_END");
        this.chunkProviderType = prop.getString() != null ? prop.getString() : "";

        prop = conf.get(category, "chunkProviderOptions", this.chunkProviderOptions);
        prop.setComment("Extra options for the ChunkProvider (used for FLAT and DEFAULT).");
        this.chunkProviderOptions = prop.getString() != null ? prop.getString() : "";

        // These config values only exist and are used in the non-per-dimension configs
        if (this.isMaster)
        {
            prop = conf.get(category, "enabledInDimensions", this.enabledInDimensions);
            prop.setComment("A list of dimensions where Painted Biomes should be enabled.");
            this.enabledInDimensions = prop.getIntList();

            prop = conf.get(category, "useBGROrderInConfig", this.useBGROrderInConfig);
            prop.setComment("If true, then the colors i nthe config are specified in BGR order instead of RGB");
            this.useBGROrderInConfig = prop.getBoolean();

            prop = conf.get(category, "useCustomColorsAsDefaults", this.useCustomColorMappings);
            prop.setComment("This only affects whether the missing ColorToBiomeMappings values, when initially added, use the custom colors\n" +
                            "from Amidst, or if they just map the Biome ID to the red channel.\n" +
                            "true = Use custom colors from Amidst as defaults, false = Map biome ID to red channel.");
            this.useCustomColorMappings = prop.getBoolean();

            prop = conf.get(category, "useGenLayer", this.useGenLayer);
            prop.setComment("Use biome GenLayer overrides instead of a WorldChunkManager wrapper.\n" +
                            "This only works in the Overworld, and not with many custom WorldTypes.\n" +
                            "This is the method used until and including the v0.3.0 release.");
            this.useGenLayer = prop.getBoolean();

            this.readColorToBiomeMappings(conf);
        }

        if (conf.hasChanged())
        {
            conf.save();
        }

        return this;
    }

    private void readColorToBiomeMappings(Configuration conf)
    {
        String colorOrder = this.useBGROrderInConfig ? "BGR" : "RGB";
        String redExample = this.useBGROrderInConfig ? "0000FF" : "FF0000";
        ConfigCategory configCategory = conf.getCategory("ColorToBiomeMappings");
        configCategory.setComment(  "Mappings from biome's registry name to the " + colorOrder + " color value.\n" +
                                    "Specified in " + colorOrder + " order, as hexadecimal strings, without the leading '0x' or '#'.\n" +
                                    "For example '" + redExample + "' for red.\n" +
                                    "To find out the biome registry names, you can use for example:\n" +
                                    "1) The TellMe mod (the command '/tellme dump biomes' will write them to a file in config/tellme/)\n" +
                                    "2) The mod MiniHUD (version 0.10.0 or later) to see the registry name of the biome you are currently in");

        ColorToBiomeMapping colorToBiomeMapping = new ColorToBiomeMapping();

        // Iterate over the biome registry and add a color-to-biome mapping for all biomes
        for (Map.Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            Biome biome = entry.getValue();

            if (biome == null)
            {
                continue;
            }

            String registryName = entry.getKey().toString();
            Property prop = null;
            int biomeId = Biome.getIdForBiome(biome);

            // Default mapping is from the Biome ID to the red channel
            int color = (biomeId & 0xFF) << 16;

            // Mapping found in the config by the biome's registry name
            if (configCategory.containsKey(registryName))
            {
                prop = configCategory.get(registryName);
            }

            if (prop != null)
            {
                try
                {
                    color = Integer.parseInt(prop.getString(), 16);

                    if (this.useBGROrderInConfig)
                    {
                        color = convertRGBAndBGR(color);
                    }
                }
                catch (NumberFormatException e)
                {
                    PaintedBiomes.logger.warn("Failed to parse color value '{}' for biome '{}'", prop.getString(), registryName);
                }
            }
            // No mapping found in the config, add a default mapping, so that all the existing biomes will get added to the config
            else
            {
                if (this.useCustomColorMappings)
                {
                    // Try to get the default custom color, if one is defined for this biome
                    Integer colorInteger = DefaultColorMappings.getColorForBiome(registryName);

                    if (colorInteger != null)
                    {
                        color = colorInteger.intValue();
                    }
                }

                int colorConfig = this.useBGROrderInConfig ? convertRGBAndBGR(color) : color;
                prop = new Property(registryName, String.format("%06X", colorConfig), Property.Type.STRING);
                configCategory.put(registryName, prop);
            }

            int oldId = colorToBiomeMapping.getBiomeIDForColor(color);

            // The color is already in use, print a warning
            if (oldId != -1)
            {
                int colorPrint = this.useBGROrderInConfig ? convertRGBAndBGR(color) : color;
                PaintedBiomes.logger.warn("**** WARNING **** WARNING **** WARNING ****");
                PaintedBiomes.logger.warn(String.format("The color %06X (%d), attempted to use for biome '%s' (ID: %d), is already in use!",
                        colorPrint, color, registryName, biomeId));
                PaintedBiomes.logger.warn("The biomes using that color are:");

                for (Biome biomeTmp : ForgeRegistries.BIOMES.getValuesCollection())
                {
                    if (biomeTmp != null && Biome.getIdForBiome(biomeTmp) == oldId)
                    {
                        PaintedBiomes.logger.warn("  Biome: '{}' (ID: {})", biomeTmp.getRegistryName(), oldId);
                    }
                }

                PaintedBiomes.logger.warn("This new color mapping HAS NOT been added to the active mappings.");
                PaintedBiomes.logger.warn("Please fix this conflict in the configuration file!");
                PaintedBiomes.logger.warn("-------------------------------------------");
            }
            else
            {
                // For simplicity, when generating terrain, the biome is always read from the mapping, even in case of a red channel mapping.
                // So basically we want to always add all the existing biomes to the color-to-biome map.
                colorToBiomeMapping.addMapping(color, biomeId);
            }

            prop.setComment(String.format("Biome: %s, ID: %d (Color as int: %d)", registryName, biomeId, color));
        }
    }

    private static int convertRGBAndBGR(int colorIn)
    {
        int colorOut = colorIn & 0xFF00;
        colorOut |= (colorIn & 0x0000FF) << 16;
        colorOut |= (colorIn & 0xFF0000) >>> 16;
        return colorOut;
    }

    private int checkAndFixConfigValueInt(String configName, Property prop, int min, int max, int defaultValue)
    {
        int value = prop.getInt();

        if (value < min || value > max)
        {
            PaintedBiomes.logger.warn("Invalid config value for '{}': '{}', setting it to '{}'", configName, value, defaultValue);
            value = defaultValue;
            prop.set(value);
        }

        return value;
    }
}
