package fi.dy.masa.paintedbiomes.image;

import net.minecraft.world.biome.BiomeGenBase;

public interface IImageReader
{
    /**
     * Checks if the given coordinates are covered by a template image.
     * 
     * @return true if the coordinates are inside an existing template image
     */
    public boolean areCoordinatesInsideTemplate(int blockX, int blockZ);

    /**
     * Returns the Biome to use for generation at the given world coordinates.
     * This takes into account the configuration values of how undefined areas and areas outside of template images are handled.
     * The defaultBiome parameter should hold the default biome from the regular terrain generator.
     * 
     * @return The BiomeGenBase to be used for the world generation
     */
    public BiomeGenBase getBiomeAt(int blockX, int blockZ, BiomeGenBase defaultBiome);
}
