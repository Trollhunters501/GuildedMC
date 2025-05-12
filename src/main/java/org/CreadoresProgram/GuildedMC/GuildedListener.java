package org.CreadoresProgram.GuildedMC;
import com.google.common.eventbus.Subscribe;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketWelcomeEvent;
import vip.floatationdevice.guilded4j.object.misc.Bot;
import cn.nukkit.Server;
import cn.nukkit.Player;
import java.util.*;
public class GuildedListener {
    private Bot selfBot;
    public GuildedListener(){
        // Constructor
    }
    @Subscribe
    public void onChatMessageCreated(ChatMessageCreatedEvent event) {
        ChatMessage message = event.getChatMessage();
        if(message.getChannelId() != Main.config.getString("channelId") || message.getCreatorId() == this.selfBot.getId()){
            return;
        }
        if(!Main.config.getBoolean("allowBotMsg") && (message.isSystemMessage() || message.isWebhookMessage())){
            return;
        }
        if(message.getContent().startsWith("prefixCommand")){
            if(Main.config.getBoolean("enableIpCommand") && message.getContent().equalsIgnoreCase(Main.config.getString("prefix") + "ip")){
                Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), ">>> Server IP:\nIP: "+ Main.config.getString("serverIp")+"\nPort: "+ Main.config.getString("serverPort"));
                return;
            }
            if(Main.config.getBoolean("enablePlayerListCommand") && message.getContent().equalsIgnoreCase(Main.config.getString("prefix") + "players")){
                if(Server.getInstance().getOnlinePlayers().isEmpty()){
                    Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), ">>> Player List:\nNo players online.");
                    return;
                }

                String playerList = ">>> Player List ("+Server.getInstance().getOnlinePlayers().size()+"/"+Server.getInstance().getMaxPlayers()+"):\n";
                List<String> players = new ArrayList<>(Server.getInstance().getOnlinePlayers().size());
                for (Player jugado : Server.getInstance().getOnlinePlayers().values()){
                    players.add(jugado.getName());
                }
                players.sort(String.CASE_INSENSITIVE_ORDER);
                StringJoiner sj = new StringJoiner(", ");
                for (String player : players) {
                    sj.add(player);
                }
                playerList += sj.toString();
                if (playerList.length() > 1996) {
                    playerList = playerList.substring(0, 1993) + "...";
                }
                Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), playerList);
                return;
            }
        }
        Server.getInstance().broadcastMessage(Main.config.getString("prefixGuilded").replace("%username%", Main.g4jclient.getMemberManager().getServerMember(message.getServerId(), message.getCreatorId()).getName()) + message.getContent());
    }
    @Subscribe
    public void onGuildedWelcome(GuildedWebSocketWelcomeEvent event) {
        if(Main.config.getBoolean("isStartingMsg")){
            Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), Main.config.getString("startingMsg"));
        }
        this.selfBot = event.getSelf();
    }
}
