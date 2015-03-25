package fi.dy.masa.paintedbiomes.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fi.dy.masa.paintedbiomes.PaintedBiomes;

public class ImageRegion
{
    public int imageX;
    public int imageZ;
    public BufferedImage imageData;
    public String name;

    public ImageRegion(int regionX, int regionZ, String path)
    {
        this.imageX = regionX;
        this.imageZ = regionZ;
        this.name = "r." + regionX + "." + regionZ + ".png";
        File imageFile = new File(path, this.name);

        try
        {
            if (imageFile.exists() == true)
            {
                this.imageData = ImageIO.read(imageFile);
            }
        }
        catch (IOException e)
        {
            this.imageData = null;
            PaintedBiomes.logger.warn("Failed to read image template from '" + imageFile.getAbsolutePath() + "'");
        }
    }

    public boolean isValidLocation(int blockX, int blockZ)
    {
        if (this.imageData == null || blockX >= this.imageData.getWidth() || blockZ >= this.imageData.getHeight())
        {
            return false;
        }

        return true;
    }

    public int getColorForCoords(int blockX, int blockZ)
    {
        blockX = (blockX % 512 + 512) % 512;
        blockZ = (blockZ % 512 + 512) % 512;

        return this.imageData.getRGB(blockX, blockZ);
    }
}
