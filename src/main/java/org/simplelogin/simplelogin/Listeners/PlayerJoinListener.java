package org.simplelogin.simplelogin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.simplelogin.simplelogin.ConfigManager;
import org.simplelogin.simplelogin.GUI.AuthGUI;
import org.simplelogin.simplelogin.SimpleLogin;

import java.util.*;

public class PlayerJoinListener implements Listener {

    private static PlayerJoinListener instance;
    private final SimpleLogin plugin;
    private final Map<UUID, Long> timeoutTasks = new HashMap<>();  // 记录超时时间戳，不再用 BukkitTask

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

        // 3. 正版自动登录（异步验证）
        if (config.isPremiumAutoLogin()) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
                boolean isPremium = checkPremium(player, config);
                // 回到主线程执行登录或打开 GUI
                Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> {
                    if (isPremium) {
                        loginSuccess(player, "§d正版验证通过，自动登录！");
                    } else {
                        freezeAndOpen(player, config);
                    }
                });
            });
            return;
        }

        // 4. 冻结并打开铁砧
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

        // 超时踢出：改为记录时间戳，由后续的延迟任务检查
        int timeout = config.getLoginTimeout();
        if (timeout > 0 && "kick".equalsIgnoreCase(config.getTimeoutAction())) {
            long kickTime = System.currentTimeMillis() + timeout * 1000L;
            timeoutTasks.put(player.getUniqueId(), kickTime);
            // 调度一个延迟任务，在超时后检查
            player.getScheduler().runDelayed(plugin, scheduledTask -> {
                if (!plugin.getPlayerManager().isLoggedIn(player)) {
                    Long expectedKickTime = timeoutTasks.get(player.getUniqueId());
                    if (expectedKickTime != null && System.currentTimeMillis() >= expectedKickTime) {
                        timeoutTasks.remove(player.getUniqueId());
                        player.kickPlayer(config.getKickMessage());
                    }
                }
            }, null, timeout * 20L);
        }

        // 延迟打开铁砧
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            if (plugin.getPlayerManager().isRegistered(player)) {
                AuthGUI.openLogin(player);
            } else {
                AuthGUI.openRegisterStep1(player);
            }
        }, null, 10L);
    }

    public void loginSuccess(Player player, String message) {
        ConfigManager config = plugin.getConfigManager();
        timeoutTasks.remove(player.getUniqueId());

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

    private boolean checkPremium(Player player, ConfigManager config) {
        try {
            java.net.URL url = new java.net.URL("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + player.getName() + "&serverId=0");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(config.getPremiumVerifyTimeout() * 1000);
            conn.setReadTimeout(config.getPremiumVerifyTimeout() * 1000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }
}