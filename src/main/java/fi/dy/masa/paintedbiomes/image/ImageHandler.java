package fi.dy.masa.paintedbiomes.image;

import fi.dy.masa.paintedbiomes.config.Configs;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;

public class ImageHandler
{
    private static TIntObjectHashMap<ImageHandler> imageHandlers = new TIntObjectHashMap<ImageHandler>();
    private ImageCache regionImageCache;
    private IImageReader singleImage;

    private static String templateBasePath;
    private String templatePath;

    private boolean useSingleTemplateImage;
    private static int timer;

    public ImageHandler(int dimension)
    {
        //File file = new File(templateBasePath, "DIM" + dimension);
        File file = new File(templateBasePath);
        if (file.exists() == false)
        {
            file.mkdirs();
        }

        this.templatePath = file.getAbsolutePath();
    }

    public static ImageHandler getImageHandler(int dimension)
    {
        ImageHandler imageHandler = imageHandlers.get(dimension);
        if (imageHandler == null)
        {
            imageHandler = new ImageHandler(dimension);
            imageHandlers.put(dimension, imageHandler);
        }

        return imageHandler;
    }

    public static void removeImageHandler(int dimension)
    {
        imageHandlers.remove(dimension);
    }

    public static void setTemplateBasePath(String path)
    {
        templateBasePath = path;
    }

    public void init()
    {
        this.useSingleTemplateImage = Configs.getInstance().useSingleTemplateImage;

        if (this.useSingleTemplateImage == true)
        {
            this.singleImage = new ImageSingle(new File(this.templatePath, "biomes.png"));
            this.regionImageCache = null;
        }
        else
        {
            this.singleImage = null;
            this.regionImageCache = new ImageCache();
        }
    }

    public static void tickTimeouts()
    {
        if (++timer >= 20)
        {
            timer = 0;
            int threshold = 300; // 5 minute timeout for non-accessed images
            TIntObjectIterator<ImageHandler> iterator = imageHandlers.iterator();

            for (int i = imageHandlers.size(); i > 0; --i)
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

    /**
     * Returns the Biome ID to use for generation at the given world coordinates.
     * This takes into account the configuration values of how undefined areas and areas outside of template images are handled.
     * The defaultBiomeID parameter should hold the Biome ID from the regular terrain generator.
     * 
     * @return The Biome ID to be used for the world generation at the given block coordinates
     */
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        if (this.useSingleTemplateImage == true)
        {
            return this.singleImage.getBiomeIDAt(blockX, blockZ, defaultBiomeID);
        }

        return this.regionImageCache.getRegionImage(blockX, blockZ, this.templatePath).getBiomeIDAt(blockX, blockZ, defaultBiomeID);
    }
}
