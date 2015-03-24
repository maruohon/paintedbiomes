package fi.dy.masa.paintedbiomes;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import fi.dy.masa.paintedbiomes.event.BiomeEvents;
import fi.dy.masa.paintedbiomes.proxy.IProxy;
import fi.dy.masa.paintedbiomes.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptableRemoteVersions="*")
public class PaintedBiomes
{
    @Instance(Reference.MOD_ID)
    public static PaintedBiomes instance;

    @SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    public static IProxy proxy;

    public static Logger logger;
    public static String configDirPath;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        configDirPath = event.getModConfigurationDirectory().getAbsolutePath().concat("/" + Reference.MOD_ID);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new BiomeEvents());
        proxy.createWorldType();
        proxy.registerProvider();
    }

    /*@EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    }*/
}
