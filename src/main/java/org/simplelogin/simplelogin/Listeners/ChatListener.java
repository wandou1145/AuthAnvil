package org.simplelogin.simplelogin.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.simplelogin.simplelogin.SimpleLogin;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // 已登录玩家正常聊天
        if (SimpleLogin.getInstance().getPlayerManager().isLoggedIn(player)) return;

        // 如果配置关闭了聊天限制，也放行
        if (!SimpleLogin.getInstance().getConfigManager().isDisableChat()) return;

        // 未登录且配置要求禁止聊天 → 拦截
        event.setCancelled(true);
        player.sendMessage("§c请先完成登录或注册后再发送消息");
    }
}