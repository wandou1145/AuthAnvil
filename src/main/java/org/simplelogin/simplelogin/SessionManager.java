package org.simplelogin.simplelogin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final SimpleLogin plugin;
    private final Map<UUID, Long> sessions = new HashMap<>();
    private final File sessionFile;
    private FileConfiguration sessionConfig;

    public SessionManager(SimpleLogin plugin) {
        this.plugin = plugin;
        this.sessionFile = new File(plugin.getDataFolder(), "sessions.yml");
        loadSessions();
    }

    private void loadSessions() {
        if (!sessionFile.exists()) {
            try {
                sessionFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sessionConfig = YamlConfiguration.loadConfiguration(sessionFile);
        for (String key : sessionConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long expiry = sessionConfig.getLong(key);
                if (System.currentTimeMillis() < expiry) {
                    sessions.put(uuid, expiry);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Long> entry : sessions.entrySet()) {
            sessionConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            sessionConfig.save(sessionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createSession(Player player) {
        int timeoutMinutes = plugin.getConfigManager().getSessionTimeout();
        if (timeoutMinutes <= 0) return;
        long expiry = System.currentTimeMillis() + (timeoutMinutes * 60L * 1000L);
        sessions.put(player.getUniqueId(), expiry);
        saveAll();
    }

    public boolean hasValidSession(Player player) {
        Long expiry = sessions.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            sessions.remove(player.getUniqueId());
            saveAll();
            return false;
        }
        return true;
    }

    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
        sessionConfig.set(player.getUniqueId().toString(), null);
        saveAll();
    }
}