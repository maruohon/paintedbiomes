package fi.dy.masa.paintedbiomes.image;

import java.io.File;
import fi.dy.masa.paintedbiomes.config.Configs;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class ImageHandler implements IImageReader
{
    private static final TIntObjectHashMap<ImageHandler> HANDLERS = new TIntObjectHashMap<ImageHandler>();
    private ImageCache regionImageCache;
    private ImageSingle singleImage;

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

    public static void removeImageHandler(int dimension)
    {
        HANDLERS.remove(dimension);
    }

    public static void setTemplateBasePaths(File pathGlobal, File pathWorld)
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

    public ImageHandler init(long seed)
    {
        Configs configs = Configs.getConfig(this.dimension);
        this.useSingleTemplateImage = configs.useSingleTemplateImage;

        this.createAndSetTemplateDir();

        // Per-region template images mode
        if (this.useSingleTemplateImage == false)
        {
            this.singleImage = null;
            this.regionImageCache = new ImageCache(seed, this.templatePath);
        }
        // Single template image mode, with some type of template repeating
        else if (configs.useTemplateRepeating)
        {
            this.singleImage = new ImageSingleRepeating(this.dimension, seed, this.templatePath);
            this.singleImage.init();
            this.regionImageCache = null;
        }
        // Single template image mode, no repeating
        else
        {
            this.singleImage = new ImageSingle(this.dimension, seed, this.templatePath);
            this.singleImage.init();
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
            TIntObjectIterator<ImageHandler> iterator = HANDLERS.iterator();

            for (int i = HANDLERS.size(); i > 0; --i)
            {
                iterator.advance();
                ImageHandler imageHandler = iterator.value();

                if (imageHandler.useSingleTemplateImage == false)
                {
                    imageHandler.regionImageCache.removeOld(threshold);
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
