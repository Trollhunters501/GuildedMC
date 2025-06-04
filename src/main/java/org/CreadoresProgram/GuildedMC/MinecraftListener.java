package org.CreadoresProgram.GuildedMC;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.Server;
import cn.nukkit.Player;

import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayInputStream;

import vip.floatationdevice.guilded4j.object.Webhook;
import org.CreadoresProgram.FaceImageMC.FaceImageMC;

import org.jsoup.Jsoup;
import org.jsoup.Connection;

import com.google.gson.Gson;

public class MinecraftListener implements Listener {
    public Map<String, Object[]> webhookPlayers = new HashMap<>();
    private Map<String, ByteArrayInputStream> avatarPlayers = new HashMap<>();
    public MinecraftListener(){
        // Constructor
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Webhook webhook = Main.g4jclient.getWebhookManager().createWebhook(Main.g4jclient.getServerChannelManager().getServerChannel(Main.config.getString("channelId")).getServerId(), event.getPlayer().getName(), Main.config.getString("channelId"));
        webhookPlayers.put(event.getPlayer().getName(), new Object[]{"https://media.guilded.gg/webhooks/"+webhook.getId()+"/"+webhook.getToken(), webhook });
        avatarPlayers.put(event.getPlayer().getName(), new ByteArrayInputStream(FaceImageMC.extractFaceAsPNG(event.getPlayer().getSkin().getSkinData().data)));
        if(Main.config.getBoolean("isJoinLeaveMsg")){
            Main.g4jclient.getChatMessageManager().createChannelMessage(Main.config.getString("channelId"), Main.config.getString("joinMsg").replace("%username%", TextFormat.clean(event.getPlayer().getName())));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Webhook webhook = (Webhook) webhookPlayers.get(event.getPlayer().getName())[1];
        Main.g4jclient.getWebhookManager().deleteWebhook(webhook.getServerId(), webhook.getId());
        webhookPlayers.remove(event.getPlayer().getName());
        avatarPlayers.remove(event.getPlayer().getName());
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
    public void onPlayerChangeSkin(PlayerChangeSkinEvent event) {
        avatarPlayers.put(event.getPlayer().getName(), new ByteArrayInputStream(FaceImageMC.extractFaceAsPNG(event.getSkin().getSkinData().data)));
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
        String msgN = Main.config.getString("chatMsg").replace("%username%", TextFormat.clean(event.getPlayer().getName())) + TextFormat.clean(message, true);
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", msgN);
        payload.put("avatar_url", "attachment://avatar.png");
        Jsoup.connect((String) webhookPlayers.get(event.getPlayer().getName())[0]).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0")
            .ignoreContentType(true).ignoreHttpErrors(true)
            .data("file", "avatar.png", avatarPlayers.get(event.getPlayer().getName()), "image/png")
            .data("payload_json", gson.toJson(payload));
    }

    private static String textFromContainer(TextContainer container) {
        if (container instanceof TranslationContainer) {
            return Server.getInstance().getLanguage().translateString(container.getText(), ((TranslationContainer) container).getParameters());
        }
        return container.getText();
    }
}
