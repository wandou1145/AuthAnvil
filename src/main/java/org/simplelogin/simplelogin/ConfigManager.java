package org.simplelogin.simplelogin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final SimpleLogin plugin;

    public ConfigManager(SimpleLogin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    private FileConfiguration get() {
        return plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    // ---- OP 免密 ----
    public boolean isOpBypass() {
        return get().getBoolean("op-bypass", true);
    }

    // ---- 正版自动登录 ----
    public boolean isPremiumAutoLogin() {
        return get().getBoolean("premium.auto-login", true);
    }

    public int getPremiumVerifyTimeout() {
        return get().getInt("premium.verify-timeout", 5);
    }

    // ---- 左侧槽位 ----
    public String getLeftSlotMaterial() {
        return get().getString("left-slot.item-material", "RED_DYE");
    }

    public String getLeftSlotName() {
        return color(get().getString("left-slot.item-name", "§c§l退出"));
    }

    public String getLeftSlotLore() {
        return color(get().getString("left-slot.item-lore", "§7点击退出游戏"));
    }

    // ---- 右侧槽位 ----
    public String getRightSlotMaterial() {
        return get().getString("right-slot.item-material", "LIME_DYE");
    }

    public String getRightSlotName() {
        return color(get().getString("right-slot.item-name", "§a§l确认"));
    }

    public String getRightSlotLore() {
        return color(get().getString("right-slot.item-lore", "§7点击确认"));
    }

    // ---- 自定义中间槽 ----
    public boolean isCustomSlotEnabled() {
        return get().getBoolean("custom-slot.enabled", false);
    }

    public String getCustomSlotMaterial() {
        return get().getString("custom-slot.item-material", "LIGHT_BLUE_DYE");
    }

    public String getCustomSlotLore() {
        return color(get().getString("custom-slot.item-lore", ""));
    }

    public List<String> getCustomSlotCommands() {
        return get().getStringList("custom-slot.commands");
    }

    // ---- 会话 ----
    public int getSessionTimeout() {
        return get().getInt("session.timeout", 1440);
    }

    // ---- 命令 ----
    public List<String> getLoginCommands() {
        return get().getStringList("login-commands");
    }

    public List<String> getRegisterCommands() {
        return get().getStringList("register-commands");
    }

    // ---- 密码 ----
    public int getPasswordMinLength() {
        return get().getInt("password.min-length", 4);
    }

    public int getPasswordMaxLength() {
        return get().getInt("password.max-length", 16);
    }

    public boolean isPasswordRequireLetter() {
        return get().getBoolean("password.require-letter", false);
    }

    public boolean isPasswordRequireNumber() {
        return get().getBoolean("password.require-number", false);
    }

    public int getMaxAttempts() {
        return get().getInt("password.max-attempts", 3);
    }

    public String getExceedAction() {
        return get().getString("password.exceed-action", "kick");
    }

    public int getTempbanDuration() {
        return get().getInt("password.tempban-duration", 30);
    }

    // ---- 限制 ----
    public boolean isDisableMovement() {
        return get().getBoolean("restrictions.disable-movement", true);
    }

    public boolean isDisableChat() {
        return get().getBoolean("restrictions.disable-chat", true);
    }

    public boolean isDisableCommands() {
        return get().getBoolean("restrictions.disable-commands", true);
    }

    public List<String> getAllowedCommands() {
        return get().getStringList("restrictions.allowed-commands");
    }

    public boolean isApplyBlindness() {
        return get().getBoolean("restrictions.apply-blindness", true);
    }

    public boolean isApplySlowness() {
        return get().getBoolean("restrictions.apply-slowness", true);
    }

    // ---- 注册 ----
    public int getMaxAccountsPerIP() {
        return get().getInt("registration.max-accounts-per-ip", 3);
    }

    public boolean isAllowRegister() {
        return get().getBoolean("registration.allow-register", true);
    }

    // ---- 超时 ----
    public int getLoginTimeout() {
        return get().getInt("force.login-timeout", 60);
    }

    public String getTimeoutAction() {
        return get().getString("force.timeout-action", "kick");
    }

    public String getKickMessage() {
        return color(get().getString("force.kick-message", "&c登录超时"));
    }

    // ---- 音效 ----
    public String getLoginSound() {
        return get().getString("effects.login-sound", "entity.player.levelup");
    }

    public String getRegisterSound() {
        return get().getString("effects.register-sound", "entity.player.levelup");
    }

    public String getWrongPasswordSound() {
        return get().getString("effects.wrong-password-sound", "entity.villager.no");
    }

    public String getLoginTitle() {
        return color(get().getString("effects.login-title", "&a欢迎回来！"));
    }

    public String getLoginSubtitle() {
        return color(get().getString("effects.login-subtitle", "&7%player%"));
    }

    // ---- 调试 ----
    public boolean isDebugEnabled() {
        return get().getBoolean("debug.enabled", false);
    }

    // ---- 工具 ----
    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}