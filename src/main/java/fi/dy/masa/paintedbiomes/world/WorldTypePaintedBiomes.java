package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldTypePaintedBiomes extends WorldType
{
    public static final WorldType PAINTEDBIOMES = new WorldTypePaintedBiomes("PAINTEDBIOMES");

    public WorldTypePaintedBiomes(String name)
    {
        super(name);
    }

    public static void init()
    {
    }

    @Override
    public WorldChunkManager getChunkManager(World world)
    {
        return new WorldChunkManagerPaintedBiomes(world);
    }
}
