package org.simplelogin.simplelogin.Listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.simplelogin.simplelogin.ConfigManager;
import org.simplelogin.simplelogin.GUI.AuthGUI;
import org.simplelogin.simplelogin.SimpleLogin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private static PlayerJoinListener instance;
    private final SimpleLogin plugin;
    private final Map<UUID, BukkitTask> timeoutTasks = new HashMap<>();

    public PlayerJoinListener(SimpleLogin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static PlayerJoinListener getInstance() { return instance; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ConfigManager config = plugin.getConfigManager();

        // 1. 有效会话自动登录
        if (plugin.getSessionManager().hasValidSession(player)) {
            loginSuccess(player, "§a检测到有效登录会话，自动登录！");
            return;
        }

        // 2. OP 免密登录
        if (config.isOpBypass() && player.isOp()) {
            loginSuccess(player, "§d管理员自动登录！");
            return;
        }

        // 3. 冻结并打开铁砧
        freezeAndOpen(player, config);
    }

    private void freezeAndOpen(Player player, ConfigManager config) {
        player.setInvulnerable(true);
        player.setInvisible(true);
        if (config.isApplyBlindness())
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 1, false, false));
        if (config.isApplySlowness())
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 255, false, false));
        player.setAllowFlight(true);
        player.setFlying(true);

        int timeout = config.getLoginTimeout();
        if (timeout > 0 && "kick".equalsIgnoreCase(config.getTimeoutAction())) {
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!plugin.getPlayerManager().isLoggedIn(player))
                    player.kickPlayer(config.getKickMessage());
            }, timeout * 20L);
            timeoutTasks.put(player.getUniqueId(), task);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getPlayerManager().isRegistered(player)) {
                AuthGUI.openLogin(player);
            } else {
                AuthGUI.openRegisterStep1(player);
            }
        }, 10L);
    }

    public void loginSuccess(Player player, String message) {
        ConfigManager config = plugin.getConfigManager();
        BukkitTask task = timeoutTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();

        plugin.getPlayerManager().setLoggedIn(player, true);
        plugin.getSessionManager().createSession(player);

        player.closeInventory();
        player.setInvulnerable(false);
        player.setInvisible(false);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.setAllowFlight(false);
        player.setFlying(false);

        if (message != null) player.sendMessage(message);

        try {
            Sound sound = org.bukkit.Registry.SOUNDS.get(
                    org.bukkit.NamespacedKey.minecraft(config.getLoginSound().toLowerCase().replace('.', '_')));
            if (sound != null) player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}

        player.sendTitle(
                config.getLoginTitle().replace("%player%", player.getName()),
                config.getLoginSubtitle().replace("%player%", player.getName()),
                10, 70, 20
        );

        for (String cmd : config.getLoginCommands())
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    cmd.replace("%player%", player.getName()));
    }
}