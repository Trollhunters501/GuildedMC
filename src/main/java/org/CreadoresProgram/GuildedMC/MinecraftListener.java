package org.CreadoresProgram.GuildedMC;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.Server;
import cn.nukkit.Player;

public class MinecraftListener implements Listener {
    public MinecraftListener(){
        // Constructor
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(Main.config.getBoolean("isJoinLeaveMsg")){
            Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), Main.config.getString("joinMsg").replace("%username%", TextFormat.clean(event.getPlayer().getName())));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(Main.config.getBoolean("isJoinLeaveMsg")){
            Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), Main.config.getString("leaveMsg").replace("%username%", TextFormat.clean(event.getPlayer().getName())));
        }
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(Main.config.getBoolean("isDeathMsg")){
            String message = TextFormat.clean(textFromContainer(event.getDeathMessage()), true).replace("@", "[at]");
            if(Main.messageFilterPattern != null){
                message = Main.messageFilterPattern.matcher(message).replaceAll(Main.config.getString("messageFilterReplace"));
            }
            if(message.trim().isEmpty()){
                return;
            }
            Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), Main.config.getString("deathMsg").replace("%username%", TextFormat.clean(((Player) event.getEntity()).getName())).replace("%deathMsg%", TextFormat.clean(message)));
        }
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        String message = event.getMessage().replace("@", "[at]");
        if(Main.messageFilterPattern != null){
            message = Main.messageFilterPattern.matcher(message).replaceAll(Main.config.getString("messageFilterReplace"));
        }
        if(message.trim().isEmpty()){
            return;
        }
        Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), Main.config.getString("chatMsg").replace("%username%", TextFormat.clean(event.getPlayer().getName())) + TextFormat.clean(message, true));
    }

    private static String textFromContainer(TextContainer container) {
        if (container instanceof TranslationContainer) {
            return Server.getInstance().getLanguage().translateString(container.getText(), ((TranslationContainer) container).getParameters());
        }
        return container.getText();
    }
}
