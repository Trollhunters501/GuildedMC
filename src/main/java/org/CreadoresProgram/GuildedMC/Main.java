package org.CreadoresProgram.GuildedMC;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

import vip.floatationdevice.guilded4j.G4JClient;

import java.util.regex.Pattern;

public class Main extends PluginBase {
    private static Main instance;
    private static G4JClient g4jclient;
    public static Config config;
    public static Pattern messageFilterPattern;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();
        String pattern = config.getString("messageFilterRegex");
        if(!pattern.isEmpty()) {
            messageFilterPattern = Pattern.compile(pattern);
        }
        g4jclient = new G4JClient(config.getString("apiKey"));
        getLogger().info("GuildedMC is loading...");
        g4jclient.registerEventListener(new GuildedListener());
        g4jclient.connectWebSocket();
    }
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new MinecraftListener(), this);
        if(config.getBoolean("guildedCommand")){
            getServer().getCommandMap().register("guilded", new GuildedCommand());
        }
        if(config.getBoolean("isReadyMsg")){
            g4jclient.getChatMessageManager().createChannelMessage(config.getString("channelId"), config.getString("readyMsg"));
        }
        getLogger().info("GuildedMC is enabled!");
    }

    @Override
    public void onDisable() {
        if(config.getBoolean("isStoppingMsg")){
            g4jclient.getChatMessageManager().createChannelMessage(config.getString("channelId"), config.getString("stoppingMsg"));
        }
        getLogger().info("GuildedMC is disabled!");
        if(config.getBoolean("isStoppedMsg")){
            g4jclient.getChatMessageManager().createChannelMessage(config.getString("channelId"), config.getString("stoppedMsg"));
        }
    }
    public static Main getInstance() {
        return instance;
    }
    public class GuildedCommand extends Command {
        public GuildedCommand() {
            super("guilded", "Guilded command");
        }
        @Override
        public boolean execute(CommandSender sender, String s, String[] args) {
            sender.sendMessage(config.getString("guildedCommandMessage"));
            return false;
        }
    }
}