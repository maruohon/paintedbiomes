package fi.dy.masa.paintedbiomes.proxy;

import net.minecraftforge.common.DimensionManager;
import fi.dy.masa.paintedbiomes.world.WorldProviderPaintedBiomes;
import fi.dy.masa.paintedbiomes.world.WorldTypePaintedBiomes;

public class CommonProxy implements IProxy
{
    @Override
    public void registerProvider()
    {
        // Unregister previous provider
        DimensionManager.unregisterProviderType(0);

        // Register our own provider
        DimensionManager.registerProviderType(0, WorldProviderPaintedBiomes.class, true);
    }

    @Override
    public void createWorldType()
    {
        new WorldTypePaintedBiomes();
    }
}
