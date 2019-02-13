package fi.dy.masa.paintedbiomes.image;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.world.WorldServer;
import fi.dy.masa.paintedbiomes.config.Configs;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ImageHandler implements IImageReader
{
    private static final Int2ObjectOpenHashMap<ImageHandler> HANDLERS = new Int2ObjectOpenHashMap<>();
    @Nullable private ImageCache regionImageCache;
    @Nullable private ImageSingle singleImage;

    private static File templateBasePathGlobal;
    private static File templateBasePathWorld;
    private File templatePath;
    private final int dimension;

    private boolean useSingleTemplateImage;
    private static int timer;

    private ImageHandler(int dimension)
    {
        this.dimension = dimension;
    }

    public static ImageHandler getImageHandler(int dimension)
    {
        ImageHandler imageHandler = HANDLERS.get(dimension);

        if (imageHandler == null)
        {
            imageHandler = new ImageHandler(dimension);
            HANDLERS.put(dimension, imageHandler);
        }

        return imageHandler;
    }

    @Nullable
    public static ImageHandler getImageHandlerIfExists(int dimension)
    {
        return HANDLERS.get(dimension);
    }

    public static void removeImageHandler(int dimension)
    {
        HANDLERS.remove(dimension);
    }

    public static void setTemplateBasePaths(File pathGlobal, @Nullable File pathWorld)
    {
        templateBasePathGlobal = pathGlobal;
        templateBasePathWorld = pathWorld;
    }

    protected void createAndSetTemplateDir()
    {
        if (templateBasePathWorld != null)
        {
            this.templatePath = new File(templateBasePathWorld, "dim" + this.dimension);
        }
        else
        {
            this.templatePath = null;
        }

        if (this.templatePath == null || this.templatePath.exists() == false || this.templatePath.isDirectory() == false)
        {
            this.templatePath = new File(templateBasePathGlobal, "dim" + this.dimension);
        }
    }

    public void onWorldLoad(WorldServer world)
    {
        if (this.singleImage != null)
        {
            this.singleImage.onWorldLoad(world);
        }
    }

    public ImageHandler init(long seed)
    {
        Configs config = Configs.getConfig(this.dimension);
        this.useSingleTemplateImage = config.useSingleTemplateImage;

        this.createAndSetTemplateDir();

        // Per-region template images mode
        if (this.useSingleTemplateImage == false)
        {
            this.singleImage = null;
            this.regionImageCache = new ImageCache(seed, this.templatePath);
        }
        // Single template image mode, with some type of template repeating
        else if (config.useTemplateRepeating)
        {
            this.singleImage = new ImageSingleRepeating(this.dimension, seed, config, this.templatePath);
            this.regionImageCache = null;
        }
        // Single template image mode, no repeating
        else
        {
            this.singleImage = new ImageSingle(this.dimension, seed, config, this.templatePath);
            this.regionImageCache = null;
        }

        return this;
    }

    public static void tickTimeouts()
    {
        if (++timer >= 200)
        {
            timer = 0;
            int threshold = 30; // 5 minute timeout for non-accessed images

            for (ImageHandler handler : HANDLERS.values())
            {
                if (handler.useSingleTemplateImage == false)
                {
                    handler.regionImageCache.removeOld(threshold);
                }
            }
        }
    }

    @Override
    public boolean isBiomeDefinedAt(int blockX, int blockZ)
    {
        if (this.useSingleTemplateImage)
        {
            return this.singleImage.isBiomeDefinedAt(blockX, blockZ);
        }

        return this.regionImageCache.getRegionImage(this.dimension, blockX, blockZ).isBiomeDefinedAt(blockX, blockZ);
    }

    @Override
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.useSingleTemplateImage)
        {
            return this.singleImage.getBiomeIDAt(blockX, blockZ, defaultBiomeID);
        }

        return this.regionImageCache.getRegionImage(this.dimension, blockX, blockZ).getBiomeIDAt(blockX, blockZ, defaultBiomeID);
    }
}
