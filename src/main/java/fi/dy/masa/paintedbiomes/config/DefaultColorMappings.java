package fi.dy.masa.paintedbiomes.config;

import java.util.Map;

import com.google.common.collect.Maps;

public class DefaultColorMappings
{
    private static Map<String, Integer> defaultColors;

    static
    {
        defaultColors = Maps.newHashMap();

        defaultColors.put("Ocean", 0x000070);
        defaultColors.put("Plains", 0x8DB360);
        defaultColors.put("Desert", 0xFA9418);
        defaultColors.put("Extreme Hills", 0x606060);
        defaultColors.put("Forest", 0x056621);
        defaultColors.put("Taiga", 0x0B6659);
        defaultColors.put("Swampland", 0x07F9B2);
        defaultColors.put("River", 0x0000FF);
        defaultColors.put("Hell", 0xFF0000);
        defaultColors.put("Sky", 0x8080FF);
        defaultColors.put("FrozenOcean", 0x9090A0);
        defaultColors.put("FrozenRiver", 0xA0A0FF);
        defaultColors.put("Ice Plains", 0xFFFFFF);
        defaultColors.put("Ice Mountains", 0xA0A0A0);
        defaultColors.put("MushroomIsland", 0xFF00FF);
        defaultColors.put("MushroomIslandShore", 0xA000FF);
        defaultColors.put("Beach", 0xFADE55);
        defaultColors.put("DesertHills", 0xD25F12);
        defaultColors.put("ForestHills", 0x22551C);
        defaultColors.put("TaigaHills", 0x163933);
        defaultColors.put("Extreme Hills Edge", 0x72789A);
        defaultColors.put("Jungle", 0x537B09);
        defaultColors.put("JungleHills", 0x2C4205);
        defaultColors.put("JungleEdge", 0x628B17);
        defaultColors.put("Deep Ocean", 0x000030);
        defaultColors.put("Stone Beach", 0xA2A284);
        defaultColors.put("Cold Beach", 0xFAF0C0);
        defaultColors.put("Birch Forest", 0x307444);
        defaultColors.put("Birch Forest Hills", 0x1F5F32);
        defaultColors.put("Roofed Forest", 0x40511A);
        defaultColors.put("Cold Taiga", 0x31554A);
        defaultColors.put("Cold Taiga Hills", 0x243F36);
        defaultColors.put("Mega Taiga", 0x596651);
        defaultColors.put("Mega Taiga Hills", 0x454F3E);
        defaultColors.put("Extreme Hills+", 0x507050);
        defaultColors.put("Savanna", 0xBDB25F);
        defaultColors.put("Savanna Plateau", 0xA79D64);
        defaultColors.put("Mesa", 0xD94515);
        defaultColors.put("Mesa Plateau F", 0xB09765);
        defaultColors.put("Mesa Plateau", 0xCA8C65);
        defaultColors.put("Sunflower Plains", 0xB5DB88);
        defaultColors.put("Desert M", 0xFFBC40);
        defaultColors.put("Extreme Hills M", 0x888888);
        defaultColors.put("Flower Forest", 0x2D8E49);
        defaultColors.put("Taiga M", 0x338E81);
        defaultColors.put("Swampland M", 0x2FFFDA);
        defaultColors.put("Ice Plains Spikes", 0xB4DCDC);
        defaultColors.put("Jungle M", 0x7BA331);
        defaultColors.put("JungleEdge M", 0x8AB33F);
        defaultColors.put("Birch Forest M", 0x589C6C);
        defaultColors.put("Birch Forest Hills M", 0x47875A);
        defaultColors.put("Roofed Forest M", 0x687942);
        defaultColors.put("Cold Taiga M", 0x597D72);
        defaultColors.put("Mega Spruce Taiga", 0x818E79);
        defaultColors.put("Mega Spruce Taiga", 0x6D7766);
        defaultColors.put("Extreme Hills+ M", 0x789878);
        defaultColors.put("Savanna M", 0xE5DA87);
        defaultColors.put("Savanna Plateau M", 0xCFC58C);
        defaultColors.put("Mesa (Bryce)", 0xFF6D3D);
        defaultColors.put("Mesa Plateau F M", 0xD8BF8D);
        defaultColors.put("Mesa Plateau M", 0xF2B48D);
    }

    public static Integer getBiomeColor(String biomeName)
    {
        return defaultColors.get(biomeName);
    }
}
