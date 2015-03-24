package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;

public class WorldProviderPaintedBiomes extends WorldProvider
{
    @Override
    public void registerWorldChunkManager()
    {
        //this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F);
        this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F);
    }

    @Override
    public IChunkProvider createChunkGenerator()
    {
        //return new ChunkProviderHell(this.worldObj, this.worldObj.getSeed());
        return new ChunkProviderEnd(this.worldObj, this.worldObj.getSeed());
    }

    @Override
    public String getDimensionName()
    {
        return "Overworld";
    }
}
