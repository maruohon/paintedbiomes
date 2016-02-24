package fi.dy.masa.paintedbiomes.image;


public interface IImageReader
{
    /**
     * Returns true if the biome at the given location is defined either by a template image, or
     * the undefinedAreaBiomeID or the unpaintedAreaBiomeID settings.
     * Will only return false, if the given location is either transparent in the template or
     * not covered by the template at all, AND the corresponding setting is to use the regular
     * terrain generation's biome.
     * @param blockX
     * @param blockZ
     * @return true if the given location does NOT use regular terrain generation's biome
     */
    public boolean isBiomeDefinedAt(int blockX, int blockZ);

    /**
     * Returns the Biome ID to use for generation at the given world coordinates.
     * This takes into account the configuration values of how undefined areas and areas outside of template images are handled.
     * The defaultBiomeID parameter should hold the Biome ID from the regular terrain generator.
     * @param blockX
     * @param blockZ
     * @param defaultBiomeID The biome ID from regular terrain generation
     * @return The Biome ID to be used for the world generation at the given block coordinates
     */
    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID);
}
