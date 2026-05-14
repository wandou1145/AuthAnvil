package org.simplelogin.simplelogin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerManager {

    private final SimpleLogin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final Map<String, Set<UUID>> ipMap = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public PlayerManager(SimpleLogin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("players") != null) {
            for (String key : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String ip = dataConfig.getString("players." + key + ".ip", "");
                    if (!ip.isEmpty()) {
                        ipMap.computeIfAbsent(ip, k -> new HashSet<>()).add(uuid);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            String key = entry.getKey().toString();
            dataConfig.set("players." + key + ".password", entry.getValue().getPassword());
            dataConfig.set("players." + key + ".registered", entry.getValue().isRegistered());
            dataConfig.set("players." + key + ".ip", entry.getValue().getIp());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerDataMap.containsKey(uuid)) {
            String password = dataConfig.getString("players." + uuid + ".password", null);
            boolean registered = dataConfig.getBoolean("players." + uuid + ".registered", false);
            String ip = dataConfig.getString("players." + uuid + ".ip", "");
            PlayerData data = new PlayerData(password, registered, ip);
            playerDataMap.put(uuid, data);
        }
        return playerDataMap.get(uuid);
    }

    public void setPlayerPassword(Player player, String password) {
        UUID uuid = player.getUniqueId();
        PlayerData data = getPlayerData(player);
        data.setPassword(password);
        data.setRegistered(true);
        String ip = player.getAddress().getAddress().getHostAddress();
        data.setIp(ip);
        ipMap.computeIfAbsent(ip, k -> new HashSet<>()).add(uuid);
        saveAll();
    }

    public void setLoggedIn(Player player, boolean loggedIn) {
        getPlayerData(player).setLoggedIn(loggedIn);
    }

    public boolean isLoggedIn(Player player) {
        return getPlayerData(player).isLoggedIn();
    }

    public boolean isRegistered(Player player) {
        return getPlayerData(player).isRegistered();
    }

    public int getAccountsCountByIP(String ip) {
        Set<UUID> uuids = ipMap.get(ip);
        return uuids == null ? 0 : uuids.size();
    }

    public static class PlayerData {
        private String password;
        private boolean registered;
        private boolean loggedIn;
        private String ip;

        public PlayerData(String password, boolean registered, String ip) {
            this.password = password;
            this.registered = registered;
            this.loggedIn = false;
            this.ip = ip;
        }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isRegistered() { return registered; }
        public void setRegistered(boolean registered) { this.registered = registered; }
        public boolean isLoggedIn() { return loggedIn; }
        public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
    }
}