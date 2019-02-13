package fi.dy.masa.paintedbiomes.image;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import fi.dy.masa.paintedbiomes.config.Configs;

public class ImageSingle extends ImageBase implements IImageReader
{
    protected final File templatePath;
    protected final int templateAlignmentMode;
    protected boolean templateAlignmentNeedsInit;
    protected int templateAlignmentX;
    protected int templateAlignmentZ;

    protected int minX;
    protected int maxX;
    protected int minZ;
    protected int maxZ;

    public ImageSingle(int dimension, long seed, Configs config, File templatePath)
    {
        super(dimension, seed, config);

        this.templatePath = templatePath;
        this.templateAlignmentMode = MathHelper.clamp(config.templateAlignmentMode, 0, 4);

        if (config.templateAlignToWorldSpawn)
        {
            this.templateAlignmentNeedsInit = true;
            this.setTemplateAlignmentToWorldSpawn();
        }
        else
        {
            this.templateAlignmentX = config.templateAlignmentX;
            this.templateAlignmentZ = config.templateAlignmentZ;
        }

        this.init();
    }

    protected void setTemplateAlignmentToWorldSpawn()
    {
        this.setTemplateAlignmentToWorldSpawn(DimensionManager.getWorld(this.dimension));
    }

    protected void setTemplateAlignmentToWorldSpawn(@Nullable WorldServer world)
    {
        if (world != null)
        {
            BlockPos spawn = world.getSpawnCoordinate();

            // In vanilla just the End dimension has a spawn coordinate
            if (spawn == null)
            {
                spawn = world.getSpawnPoint();
            }

            this.templateAlignmentX = spawn.getX();
            this.templateAlignmentZ = spawn.getZ();
            this.templateAlignmentNeedsInit = false;
        }
    }

    public void init()
    {
        this.setTemplateTransformations(this.templateAlignmentX, this.templateAlignmentZ);
        this.readTemplateImage(this.templatePath);
    }

    public void onWorldLoad(WorldServer world)
    {
        if (this.templateAlignmentNeedsInit)
        {
            this.setTemplateAlignmentToWorldSpawn(world);
            this.init();
        }
    }

    protected void readTemplateImage(File templatePath)
    {
        File templateFile = new File(templatePath, "biomes.png");

        if (this.useAlternateTemplates)
        {
            File tmpFile = new File(templatePath, "biomes_alt_" + (this.alternateTemplate + 1) + ".png");

            if (tmpFile.exists() && tmpFile.isFile())
            {
                templateFile = tmpFile;
            }
        }

        this.imageData = this.readImageData(templateFile);
        this.setTemplateDimensions();
        this.setAreaBounds();
    }

    protected void setAreaBounds()
    {
        // Centered
        if (this.templateAlignmentMode == 0)
        {
            this.minX = this.templateAlignmentX - (this.areaSizeX / 2);
            this.minZ = this.templateAlignmentZ - (this.areaSizeZ / 2);
            this.maxX = this.templateAlignmentX + (int)Math.ceil(((double)this.areaSizeX / 2.0d)) - 1;
            this.maxZ = this.templateAlignmentZ + (int)Math.ceil(((double)this.areaSizeZ / 2.0d)) - 1;
        }
        // The top left corner is at the set coordinates
        else if (this.templateAlignmentMode == 1)
        {
            this.minX = this.templateAlignmentX;
            this.minZ = this.templateAlignmentZ;
            this.maxX = this.minX + this.areaSizeX - 1;
            this.maxZ = this.minZ + this.areaSizeZ - 1;
        }
        // The top right corner is at the set coordinates
        else if (this.templateAlignmentMode == 2)
        {
            this.minX = this.templateAlignmentX - this.areaSizeX;
            this.minZ = this.templateAlignmentZ;
            this.maxX = this.templateAlignmentX - 1;
            this.maxZ = this.minZ + this.areaSizeZ - 1;
        }
        // The bottom right corner is at the set coordinates
        else if (this.templateAlignmentMode == 3)
        {
            this.minX = this.templateAlignmentX - this.areaSizeX;
            this.minZ = this.templateAlignmentZ - this.areaSizeZ;
            this.maxX = this.templateAlignmentX - 1;
            this.maxZ = this.templateAlignmentZ - 1;
        }
        // The bottom left corner is at the set coordinates
        else if (this.templateAlignmentMode == 4)
        {
            this.minX = this.templateAlignmentX;
            this.minZ = this.templateAlignmentZ - this.areaSizeZ;
            this.maxX = this.minX + this.areaSizeX - 1;
            this.maxZ = this.templateAlignmentZ - 1;
        }
    }

    @Override
    protected int getAreaX(int blockX)
    {
        return blockX - this.minX;
    }

    @Override
    protected int getAreaZ(int blockZ)
    {
        return blockZ - this.minZ;
    }

    @Override
    protected boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return this.imageData != null && blockX >= this.minX && blockX <= this.maxX && blockZ >= this.minZ && blockZ <= this.maxZ;
    }
}
