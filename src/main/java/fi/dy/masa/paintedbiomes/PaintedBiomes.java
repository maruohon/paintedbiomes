package fi.dy.masa.paintedbiomes;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.event.PaintedBiomesEventHandler;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.reference.Reference;
import fi.dy.masa.paintedbiomes.world.WorldTypePaintedBiomes;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptableRemoteVersions="*")
public class PaintedBiomes
{
    @Instance(Reference.MOD_ID)
    public static PaintedBiomes instance;

    //@SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    //public static IProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        Configs.init(event.getSuggestedConfigurationFile());
        ImageHandler.setTemplateBasePath(new File(event.getModConfigurationDirectory(), Reference.MOD_ID).getAbsolutePath());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Load the configs later in the init cycle so that the Biome mods have a chance to register their biomes first
        Configs.getInstance().loadConfigs();

        //MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeEvents());
        PaintedBiomesEventHandler handler = new PaintedBiomesEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
        WorldTypePaintedBiomes.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        Configs.getInstance().loadConfigs();
    }
}
