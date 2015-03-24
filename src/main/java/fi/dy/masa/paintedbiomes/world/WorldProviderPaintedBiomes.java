package fi.dy.masa.paintedbiomes.world;

/*import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderPaintedBiomes extends WorldProvider
{
    @Override
    public void registerWorldChunkManager()
    {
        //this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F);
        //this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F);
        this.worldChunkMgr = new WorldChunkManagerPaintedBiomes(this.worldObj);
        this.dimensionId = 0;
    }

    @Override
    public IChunkProvider createChunkGenerator()
    {
        //return new ChunkProviderHell(this.worldObj, this.worldObj.getSeed());
        //return new ChunkProviderEnd(this.worldObj, this.worldObj.getSeed());
        return terrainType.getChunkGenerator(this.worldObj, this.field_82913_c);
    }

    @Override
    public String getDimensionName()
    {
        return "Overworld";
    }
}*/
