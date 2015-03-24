package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import fi.dy.masa.paintedbiomes.image.ColorToBiomeMapping;
import fi.dy.masa.paintedbiomes.image.ImageCache;
import fi.dy.masa.paintedbiomes.image.ImageRegion;

public class WorldChunkManagerPaintedBiomes extends WorldChunkManager
{
    //private GenLayer genBiomes;
    /** A GenLayer containing the indices into BiomeGenBase.biomeList[] */
    private GenLayer biomeIndexLayer;

    public WorldChunkManagerPaintedBiomes(World world)
    {
        super(world);
        GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(world.getSeed(), world.getWorldInfo().getTerrainType());
        agenlayer = getModdedBiomeGenerators(world.getWorldInfo().getTerrainType(), world.getSeed(), agenlayer);
        //this.genBiomes = agenlayer[0];
        this.biomeIndexLayer = agenlayer[1];
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int length, boolean cacheFlag)
    {
        /*Iterator<Entry<Integer, BiomeGenBase>> iter = ColorToBiomeMapping.instance.customMappings.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<Integer, BiomeGenBase> entry = iter.next();
            System.out.println("key: " + String.format("0x%08X", entry.getKey()) + " value: " + (entry.getValue() != null ? entry.getValue().biomeName : "null"));
        }*/

        // Get cached biomes
        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0)
        {
            return super.getBiomeGenAt(biomes, x, z, width, length, cacheFlag);
        }
        else
        {
            //ImageCache.instance.loadRange(x, z, width, length);

            int len = width * length;
            if (biomes == null || biomes.length < len)
            {
                biomes = new BiomeGenBase[len];
            }

            int endX = x + width;
            int endZ = z + length;
            int[] aint = this.biomeIndexLayer.getInts(x, z, width, length);

            for (int i = 0, bz = z; bz < endZ; ++bz)
            {
                for (int bx = x; bx < endX; ++bx)
                {
                    ImageRegion ir = ImageCache.instance.getImageRegion(bx, bz);
                    // If the requested location is mapped on a template image
                    if (ir.isValidLocation(bx, bz) == true)
                    {
                        int color = ir.getColorForCoords(bx, bz);
                        biomes[i] = ColorToBiomeMapping.instance.getBiomeForColor(color);
                        //System.out.println("ir.getColorForCoords(" + bx + ", " + bz + "): " + String.format("0x%08X", color) + " biome: " + (biomes[i] != null ? biomes[i].biomeName : "null"));
                        if (biomes[i] == null)
                        {
                            biomes[i] = BiomeGenBase.getBiome(aint[i]);
                            //System.out.println("getBiomeForColor(): null");
                        }
                    }
                    else
                    {
                        biomes[i] = BiomeGenBase.getBiome(aint[i]);
                        if (biomes[i] == null)
                        {
                            //System.out.println("getInts(): null");
                        }
                    }

                    i++;
                }
            }

            /*for (int i = 0; i < len; ++i)
            {
                if (Math.abs(x) < 32 && Math.abs(z) < 32)
                {
                    biomes[i] = BiomeGenBase.megaTaiga;
                }
                else
                {
                    biomes[i] = BiomeGenBase.birchForestHills;
                }
            }*/
        }

        return biomes;
    }
}
