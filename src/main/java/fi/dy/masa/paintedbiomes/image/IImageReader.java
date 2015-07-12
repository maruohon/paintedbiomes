package fi.dy.masa.paintedbiomes.image;


public interface IImageReader
{
    /**
     * Returns the Biome ID to use for generation at the given world coordinates.
     * This takes into account the configuration values of how undefined areas and areas outside of template images are handled.
     * The defaultBiomeID parameter should hold the Biome ID from the regular terrain generator.
     * 
     * @return The Biome ID to be used for the world generation at the given block coordinates
     */
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID);
}
