package com.obama69.mcauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.obama69.mcauth.mc.WorldAuthSession;
import com.obama69.mcauth.mc.commands.RegisterCommandsEventListener;
import com.obama69.mcauth.mc.player.InterceptedEvents;
import com.obama69.mcauth.mc.player.PlayerLoginEventListener;
import com.obama69.mcauth.mc.player.PlayerLogoutEventListener;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mcauth")
public class MCAuth {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static MCAuth instance;
    
    private WorldAuthSession session;
    
    public WorldAuthSession getSession() {
    	return session;
    }

    public MCAuth() {
    	instance = this;

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RegisterCommandsEventListener());
        MinecraftForge.EVENT_BUS.register(new PlayerLoginEventListener());
        MinecraftForge.EVENT_BUS.register(new PlayerLogoutEventListener());
        MinecraftForge.EVENT_BUS.register(new InterceptedEvents());
        
        session = null;
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("Starting authentication middleware");
        
        session = new WorldAuthSession(5);
    }
}
