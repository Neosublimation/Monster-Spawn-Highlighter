package com.github.lunatrius.msh;

import com.github.lunatrius.core.version.VersionChecker;
import com.github.lunatrius.msh.entity.SpawnCondition;
import com.github.lunatrius.msh.handler.ConfigurationHandler;
import com.github.lunatrius.msh.proxy.CommonProxy;
import com.github.lunatrius.msh.reference.Reference;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY)
public class MonsterSpawnHighlighter {
    @Instance(Reference.MODID)
    public static MonsterSpawnHighlighter instance;

    @SidedProxy(serverSide = Reference.PROXY_SERVER, clientSide = Reference.PROXY_CLIENT)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Reference.logger = event.getModLog();
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        proxy.setConfigEntryClasses();

        VersionChecker.registerMod(Loader.instance().getIndexedModList().get(Reference.MODID), Reference.FORGE);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerEvents();
        proxy.registerKeybindings();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        SpawnCondition.populateData();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        try {
            Reference.hasSeed = true;
            Reference.seed = event.getServer().worldServerForDimension(0).getSeed();
        } catch (Exception e) {
            Reference.hasSeed = false;
        }
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        Reference.hasSeed = false;
    }
}
