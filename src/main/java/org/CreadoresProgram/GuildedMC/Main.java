package org.CreadoresProgram.GuildedMC;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.object.Webhook;

import java.util.regex.Pattern;
import java.util.Map;

public class Main extends PluginBase {
    private static Main instance;
    public static G4JClient g4jclient;
    public static Config config;
    public static Pattern messageFilterPattern;
    private MinecraftListener minecraftListener;

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
        this.minecraftListener = new MinecraftListener();
        this.getServer().getPluginManager().registerEvents(this.minecraftListener, this);
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
        for(Map.Entry<String, Object[]> entry : this.minecraftListener.webhookPlayers.entrySet()){
            Object[] data = entry.getValue();
            Webhook webhook = (Webhook) data[1];
            g4jclient.getWebhookManager().deleteWebhook(webhook.getServerId(), webhook.getId());
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
