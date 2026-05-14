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

        // 如果正在打开铁砧（库内部触发的虚假关闭），忽略
        if (AuthGUI.openingPlayers.contains(player.getUniqueId())) return;

        String title = event.getView().getTitle();
        String cached = passwordCache.get(player.getUniqueId());

        // 延迟 1 tick 重新打开
        SimpleLogin plugin = SimpleLogin.getInstance();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!plugin.getPlayerManager().isLoggedIn(player)) {
                if (title.contains("请输入密码")) {
                    AuthGUI.openLogin(player, cached);
                } else if (title.contains("设置密码")) {
                    AuthGUI.openRegisterStep1(player, cached);
                } else if (title.contains("再次输入密码")) {
                    AuthGUI.openRegisterStep2(player, cached);
                }
            }
        });
    }
}