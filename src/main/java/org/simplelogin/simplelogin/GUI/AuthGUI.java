package org.simplelogin.simplelogin.GUI;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.simplelogin.simplelogin.ConfigManager;
import org.simplelogin.simplelogin.PasswordManager;
import org.simplelogin.simplelogin.PlayerManager;
import org.simplelogin.simplelogin.SimpleLogin;
import org.simplelogin.simplelogin.Listeners.CloseProtector;
import org.simplelogin.simplelogin.Listeners.PlayerJoinListener;

import java.util.*;

public class AuthGUI {

    private static final Map<UUID, String> pendingFirstPass = new HashMap<>();
    private static final Map<UUID, Integer> attemptCount = new HashMap<>();
    public static final Set<UUID> openingPlayers = Collections.synchronizedSet(new HashSet<>());

    private static String getInvisibleName() {
        try {
            String versionString = Bukkit.getMinecraftVersion();
            String[] parts = versionString.split("\\.");
            if (parts.length >= 2) {
                double majorVersion = Double.parseDouble(parts[0] + "." + parts[1]);
                if (majorVersion >= 1.20) return " ";
            }
        } catch (Exception ignored) {}
        return "§r";
    }

    public static void openLogin(Player player) { openLogin(player, null); }

    public static void openLogin(Player player, String prefill) {
        SimpleLogin plugin = SimpleLogin.getInstance();
        ConfigManager config = plugin.getConfigManager();
        ItemStack left = createItem(Material.RED_DYE, getInvisibleName(), config.getLeftSlotLore());
        ItemStack right = createItem(Material.LIME_DYE, getInvisibleName(), config.getRightSlotLore());

        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .plugin(plugin)
                .title("§6✦ 请输入密码 ✦")
                .itemLeft(left)
                .itemRight(right)
                .text(prefill != null && !prefill.isEmpty() ? prefill : "")
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                        player.kickPlayer("§c你选择了退出游戏");
                        return Collections.emptyList();
                    }
                    if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                        String password = stateSnapshot.getText().trim();
                        CloseProtector.passwordCache.remove(player.getUniqueId());
                        if (password.isEmpty()) {
                            player.sendMessage("§c请在输入框中输入密码！");
                            return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openLogin(player)));
                        }
                        handleLogin(player, password);
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        handleCustomSlot(player, plugin);
                        return Collections.emptyList();
                    }
                    return Collections.emptyList();
                });

        if (config.isCustomSlotEnabled()) {
            builder.itemOutput(createItem(Material.LIGHT_BLUE_DYE, getInvisibleName(), config.getCustomSlotLore()));
        } else {
            builder.itemOutput(createItem(Material.AIR, " ", ""));
        }

        openingPlayers.add(player.getUniqueId());
        builder.open(player);
        openingPlayers.remove(player.getUniqueId());
    }

    public static void openRegisterStep1(Player player) { openRegisterStep1(player, null); }

    public static void openRegisterStep1(Player player, String prefill) {
        SimpleLogin plugin = SimpleLogin.getInstance();
        ConfigManager config = plugin.getConfigManager();
        ItemStack left = createItem(Material.RED_DYE, getInvisibleName(), config.getLeftSlotLore());
        ItemStack right = createItem(Material.LIME_DYE, getInvisibleName(), config.getRightSlotLore());

        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .plugin(plugin)
                .title("§6✦ 设置密码 ✦")
                .itemLeft(left)
                .itemRight(right)
                .text(prefill != null && !prefill.isEmpty() ? prefill : "")
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                        player.kickPlayer("§c你选择了退出游戏");
                        return Collections.emptyList();
                    }
                    if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                        String password = stateSnapshot.getText().trim();
                        CloseProtector.passwordCache.remove(player.getUniqueId());
                        if (password.isEmpty()) {
                            player.sendMessage("§c请输入密码！");
                            return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openRegisterStep1(player)));
                        }
                        PasswordManager pm = plugin.getPasswordManager();
                        String validation = pm.validatePassword(password, config);
                        if (validation != null) {
                            player.sendMessage(validation);
                            return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openRegisterStep1(player)));
                        }
                        pendingFirstPass.put(player.getUniqueId(), password);
                        player.sendMessage("§e请再次输入相同的密码");
                        return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openRegisterStep2(player)));
                    }
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        handleCustomSlot(player, plugin);
                        return Collections.emptyList();
                    }
                    return Collections.emptyList();
                });

        if (config.isCustomSlotEnabled()) {
            builder.itemOutput(createItem(Material.LIGHT_BLUE_DYE, getInvisibleName(), config.getCustomSlotLore()));
        } else {
            builder.itemOutput(createItem(Material.AIR, " ", ""));
        }

        openingPlayers.add(player.getUniqueId());
        builder.open(player);
        openingPlayers.remove(player.getUniqueId());
    }

    public static void openRegisterStep2(Player player) { openRegisterStep2(player, null); }

    public static void openRegisterStep2(Player player, String prefill) {
        SimpleLogin plugin = SimpleLogin.getInstance();
        ConfigManager config = plugin.getConfigManager();
        ItemStack left = createItem(Material.RED_DYE, getInvisibleName(), config.getLeftSlotLore());
        ItemStack right = createItem(Material.LIME_DYE, getInvisibleName(), config.getRightSlotLore());

        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .plugin(plugin)
                .title("§6✦ 再次输入密码 ✦")
                .itemLeft(left)
                .itemRight(right)
                .text(prefill != null && !prefill.isEmpty() ? prefill : "")
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                        player.kickPlayer("§c你选择了退出游戏");
                        return Collections.emptyList();
                    }
                    if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                        String password = stateSnapshot.getText().trim();
                        CloseProtector.passwordCache.remove(player.getUniqueId());
                        if (password.isEmpty()) {
                            player.sendMessage("§c请输入密码！");
                            return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openRegisterStep2(player)));
                        }
                        String firstPass = pendingFirstPass.remove(player.getUniqueId());
                        if (firstPass == null) {
                            player.sendMessage("§c注册超时，请重新开始");
                            return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openRegisterStep1(player)));
                        }
                        if (!password.equals(firstPass)) {
                            player.sendMessage("§c两次密码不一致！请重新注册。");
                            return Collections.singletonList(AnvilGUI.ResponseAction.run(() -> openRegisterStep1(player)));
                        }
                        PasswordManager pm = plugin.getPasswordManager();
                        PlayerManager playerManager = plugin.getPlayerManager();
                        String hashed = pm.hashPassword(password);
                        playerManager.setPlayerPassword(player, hashed);
                        player.sendMessage("§a注册成功！");
                        PlayerJoinListener.getInstance().loginSuccess(player, null);
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        handleCustomSlot(player, plugin);
                        return Collections.emptyList();
                    }
                    return Collections.emptyList();
                });

        if (config.isCustomSlotEnabled()) {
            builder.itemOutput(createItem(Material.LIGHT_BLUE_DYE, getInvisibleName(), config.getCustomSlotLore()));
        } else {
            builder.itemOutput(createItem(Material.AIR, " ", ""));
        }

        openingPlayers.add(player.getUniqueId());
        builder.open(player);
        openingPlayers.remove(player.getUniqueId());
    }

    private static void handleCustomSlot(Player player, SimpleLogin plugin) {
        ConfigManager config = plugin.getConfigManager();
        List<String> commands = config.getCustomSlotCommands();
        if (commands == null || commands.isEmpty()) {
            player.sendMessage("§7该按钮暂未绑定任何功能。");
            return;
        }
        for (String cmd : commands) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    cmd.replace("%player%", player.getName()));
        }
    }

    private static void handleLogin(Player player, String password) {
        SimpleLogin plugin = SimpleLogin.getInstance();
        PlayerManager playerManager = plugin.getPlayerManager();
        PasswordManager passwordManager = plugin.getPasswordManager();
        ConfigManager config = plugin.getConfigManager();

        if (!playerManager.isRegistered(player)) {
            player.sendMessage("§c你还没有注册，请先注册！");
            openRegisterStep1(player);
            return;
        }

        if (passwordManager.verifyPassword(password, playerManager.getPlayerData(player).getPassword())) {
            PlayerJoinListener.getInstance().loginSuccess(player, "§a登录成功！");
        } else {
            int attempts = attemptCount.getOrDefault(player.getUniqueId(), 0) + 1;
            attemptCount.put(player.getUniqueId(), attempts);
            player.sendMessage("§c密码错误！剩余尝试次数：" + (config.getMaxAttempts() - attempts));
            if (attempts >= config.getMaxAttempts()) {
                attemptCount.remove(player.getUniqueId());
                player.kickPlayer("§c密码错误次数过多，请重新连接");
                return;
            }
            openLogin(player);
        }
    }

    private static ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}