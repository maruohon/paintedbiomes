package fi.dy.masa.paintedbiomes.image;


public interface IImageReader
{
    /**
     * Checks if the given coordinates are covered by a template image.
     * 
     * @return true if the coordinates are inside an existing template image
     */
    public boolean areCoordinatesInsideTemplate(int blockX, int blockZ);

    /**
     * Returns the Biome ID to use for generation at the given world coordinates.
     * This takes into account the configuration values of how undefined areas and areas outside of template images are handled.
     * The defaultBiomeID parameter should hold the default biome from the regular terrain generator.
     * 
     * @return The BiomeGenBase to be used for the world generation
     */
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID);
}
