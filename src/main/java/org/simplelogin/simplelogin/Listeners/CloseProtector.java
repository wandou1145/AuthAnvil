package org.simplelogin.simplelogin.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.simplelogin.simplelogin.GUI.AuthGUI;
import org.simplelogin.simplelogin.SimpleLogin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CloseProtector implements Listener {

    public static final Map<UUID, String> passwordCache = new HashMap<>();

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (SimpleLogin.getInstance().getPlayerManager().isLoggedIn(player)) return;
        if (AuthGUI.openingPlayers.contains(player.getUniqueId())) return;

        String title = event.getView().getTitle();
        String cached = passwordCache.get(player.getUniqueId());

        SimpleLogin plugin = SimpleLogin.getInstance();
        // 使用玩家调度器延迟 1 tick 重新打开铁砧
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            if (!plugin.getPlayerManager().isLoggedIn(player)) {
                if (title.contains("请输入密码")) {
                    AuthGUI.openLogin(player, cached);
                } else if (title.contains("设置密码")) {
                    AuthGUI.openRegisterStep1(player, cached);
                } else if (title.contains("再次输入密码")) {
                    AuthGUI.openRegisterStep2(player, cached);
                }
            }
        }, null, 1L);
    }
}