package fi.dy.masa.paintedbiomes;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.event.PaintedBiomesEventHandler;
import fi.dy.masa.paintedbiomes.image.ImageHandler;
import fi.dy.masa.paintedbiomes.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptableRemoteVersions="*")
public class PaintedBiomes
{
    @Instance(Reference.MOD_ID)
    public static PaintedBiomes instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        Configs.init(event.getSuggestedConfigurationFile());
        ImageHandler.setTemplateBasePath(new File(new File(event.getModConfigurationDirectory(), Reference.MOD_ID), "templates").getAbsolutePath());

        PaintedBiomesEventHandler handler = new PaintedBiomesEventHandler();
        MinecraftForge.TERRAIN_GEN_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // This is somewhat redundant, but let's do it anyway to generate the config at startup, if it's missing
        Configs.getInstance().loadConfigs();
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        // (Re-)Load the config when the server is about to start.
        // This means that in single player you can just save and exit to main menu,
        // make changes to the config and then load a world and have the new configs be used.
        Configs.getInstance().loadConfigs();
    }
}
