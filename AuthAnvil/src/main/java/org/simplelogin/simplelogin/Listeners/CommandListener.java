package org.simplelogin.simplelogin.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.simplelogin.simplelogin.SimpleLogin;

public class CommandListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (SimpleLogin.getInstance().getPlayerManager().isLoggedIn(player)) return;
        if (!SimpleLogin.getInstance().getConfigManager().isDisableCommands()) return;

        String cmd = event.getMessage().toLowerCase().split(" ")[0];
        for (String allowed : SimpleLogin.getInstance().getConfigManager().getAllowedCommands()) {
            if (cmd.equalsIgnoreCase(allowed)) return;
        }

        event.setCancelled(true);
        player.sendMessage("§c请先完成登录或注册后再使用命令");
    }
}