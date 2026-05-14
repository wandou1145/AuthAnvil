package org.simplelogin.simplelogin;

import org.simplelogin.simplelogin.Listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleLogin extends JavaPlugin {

    private static SimpleLogin instance;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private PasswordManager passwordManager;
    private SessionManager sessionManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        passwordManager = new PasswordManager();
        sessionManager = new SessionManager(this);
        playerManager = new PlayerManager(this);

        // 注册监听器
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new CloseProtector(), this);

        // 注册命令
        getCommand("authanvil").setExecutor((sender, command, label, args) -> {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                configManager.reload();
                sender.sendMessage("§aAuthAnvil 配置文件已重载！");
                return true;
            }
            sender.sendMessage("§aAuthAnvil v2.0.0 运行正常！");
            return true;
        });

        getLogger().info("AuthAnvil v2.0.0 已启用！");
    }

    @Override
    public void onDisable() {
        if (playerManager != null) playerManager.saveAll();
        if (sessionManager != null) sessionManager.saveAll();
        getLogger().info("AuthAnvil 已禁用！");
    }

    public static SimpleLogin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public PasswordManager getPasswordManager() { return passwordManager; }
    public SessionManager getSessionManager() { return sessionManager; }
}