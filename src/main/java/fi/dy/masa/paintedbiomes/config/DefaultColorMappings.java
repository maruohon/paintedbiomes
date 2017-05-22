package fi.dy.masa.paintedbiomes.config;

import java.util.Map;
import com.google.common.collect.Maps;

public class DefaultColorMappings
{
    private static Map<String, Integer> defaultColors;

    /* These are the biome colors used by default in the Amidst program. */
    static
    {
        defaultColors = Maps.newHashMap();

        defaultColors.put("minecraft:ocean",                            0x000070);
        defaultColors.put("minecraft:plains",                           0x8DB360);
        defaultColors.put("minecraft:desert",                           0xFA9418);
        defaultColors.put("minecraft:extreme_hills",                    0x606060);
        defaultColors.put("minecraft:forest",                           0x056621);
        defaultColors.put("minecraft:taiga",                            0x0B6659);
        defaultColors.put("minecraft:swampland",                        0x07F9B2);
        defaultColors.put("minecraft:river",                            0x0000FF);
        defaultColors.put("minecraft:hell",                             0xFF0000);
        defaultColors.put("minecraft:sky",                              0x8080FF);
        defaultColors.put("minecraft:frozen_ocean",                     0x9090A0);
        defaultColors.put("minecraft:frozen_river",                     0xA0A0FF);
        defaultColors.put("minecraft:ice_flats",                        0xFFFFFF);
        defaultColors.put("minecraft:ice_mountains",                    0xA0A0A0);
        defaultColors.put("minecraft:mushroom_island",                  0xFF00FF);
        defaultColors.put("minecraft:mushroom_island_shore",            0xA000FF);
        defaultColors.put("minecraft:beaches",                          0xFADE55);
        defaultColors.put("minecraft:desert_hills",                     0xD25F12);
        defaultColors.put("minecraft:forest_hills",                     0x22551C);
        defaultColors.put("minecraft:taiga_hills",                      0x163933);
        defaultColors.put("minecraft:smaller_extreme_hills",            0x72789A);
        defaultColors.put("minecraft:jungle",                           0x537B09);
        defaultColors.put("minecraft:jungle_hills",                     0x2C4205);
        defaultColors.put("minecraft:jungle_edge",                      0x628B17);
        defaultColors.put("minecraft:deep_ocean",                       0x000030);
        defaultColors.put("minecraft:stone_beach",                      0xA2A284);
        defaultColors.put("minecraft:cold_beach",                       0xFAF0C0);
        defaultColors.put("minecraft:birch_forest",                     0x307444);
        defaultColors.put("minecraft:birch_forest_hills",               0x1F5F32);
        defaultColors.put("minecraft:roofed_forest",                    0x40511A);
        defaultColors.put("minecraft:taiga_cold",                       0x31554A);
        defaultColors.put("minecraft:taiga_cold_hills",                 0x243F36);
        defaultColors.put("minecraft:redwood_taiga",                    0x596651);
        defaultColors.put("minecraft:redwood_taiga_hills",              0x454F3E);
        defaultColors.put("minecraft:extreme_hills_with_trees",         0x507050);
        defaultColors.put("minecraft:savanna",                          0xBDB25F);
        defaultColors.put("minecraft:savanna_rock",                     0xA79D64);
        defaultColors.put("minecraft:mesa",                             0xD94515);
        defaultColors.put("minecraft:mesa_rock",                        0xB09765);
        defaultColors.put("minecraft:mesa_clear_rock",                  0xCA8C65);
        defaultColors.put("minecraft:void",                             0x000000); // Not in Amidst as of v4.3-beta1
        defaultColors.put("minecraft:mutated_plains",                   0xB5DB88);
        defaultColors.put("minecraft:mutated_desert",                   0xFFBC40);
        defaultColors.put("minecraft:mutated_extreme_hills",            0x888888);
        defaultColors.put("minecraft:mutated_forest",                   0x2D8E49);
        defaultColors.put("minecraft:mutated_taiga",                    0x338E81);
        defaultColors.put("minecraft:mutated_swampland",                0x2FFFDA);
        defaultColors.put("minecraft:mutated_ice_flats",                0xB4DCDC);
        defaultColors.put("minecraft:mutated_jungle",                   0x7BA331);
        defaultColors.put("minecraft:mutated_jungle_edge",              0x8AB33F);
        defaultColors.put("minecraft:mutated_birch_forest",             0x589C6C);
        defaultColors.put("minecraft:mutated_birch_forest_hills",       0x47875A);
        defaultColors.put("minecraft:mutated_roofed_forest",            0x687942);
        defaultColors.put("minecraft:mutated_taiga_cold",               0x597D72);
        defaultColors.put("minecraft:mutated_redwood_taiga",            0x818E79);
        defaultColors.put("minecraft:mutated_redwood_taiga_hills",      0x6D7766);
        defaultColors.put("minecraft:mutated_extreme_hills_with_trees", 0x789878);
        defaultColors.put("minecraft:mutated_savanna",                  0xE5DA87);
        defaultColors.put("minecraft:mutated_savanna_rock",             0xCFC58C);
        defaultColors.put("minecraft:mutated_mesa",                     0xFF6D3D);
        defaultColors.put("minecraft:mutated_mesa_rock",                0xD8BF8D);
        defaultColors.put("minecraft:mutated_mesa_clear_rock",          0xF2B48D);
    }

    public static Integer getColorForBiome(String biomeName)
    {
        return defaultColors.get(biomeName);
    }
}
