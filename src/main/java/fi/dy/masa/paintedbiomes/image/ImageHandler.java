package fi.dy.masa.paintedbiomes.image;

import fi.dy.masa.paintedbiomes.config.Configs;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;

import net.minecraft.world.biome.BiomeGenBase;

public class ImageHandler implements IImageReader
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
        File file = new File(templateBasePath, "DIM" + dimension);
        if (file.exists() == false)
        {
            file.mkdirs();
        }

        this.templatePath = file.getAbsolutePath();

        this.init();
        imageHandlers.put(dimension, this);
    }

    public static ImageHandler getImageHandler(int dimension)
    {
        ImageHandler imageHandler = imageHandlers.get(dimension);
        if (imageHandler == null)
        {
            return new ImageHandler(dimension);
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
            this.regionImageCache = new ImageCache();
            this.singleImage = null;
        }
    }

    public static void tickTimeouts()
    {
        if (++timer >= 200)
        {
            timer = 0;
            int threshold = 60; // 60 second timeout for non-accessed images
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

    @Override
    public boolean areCoordinatesInsideTemplate(int blockX, int blockZ)
    {
        if (this.useSingleTemplateImage == true)
        {
            return this.singleImage.areCoordinatesInsideTemplate(blockX, blockZ);
        }

        return this.regionImageCache.getRegionImage(blockX, blockZ, this.templatePath).areCoordinatesInsideTemplate(blockX, blockZ);
    }

    @Override
    public BiomeGenBase getBiomeAt(int blockX, int blockZ, BiomeGenBase defaultBiome)
    {
        if (this.useSingleTemplateImage == true)
        {
            return this.singleImage.getBiomeAt(blockX, blockZ, defaultBiome);
        }

        return this.regionImageCache.getRegionImage(blockX, blockZ, this.templatePath).getBiomeAt(blockX, blockZ, defaultBiome);
    }
}
