package com.wiss.wiss.wantivpn;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Getter
public final class Config {
    private final String language;
    private final String endpoint;
    private final int timeoutMs;
    private final long cacheSeconds;
    private final int cacheMaxEntries;
    private final boolean failOpen;
    private final boolean blockProxy;
    private final boolean blockHosting;
    private final Set<String> blockedAsns;
    private final boolean webhookEnabled;
    private final String webhookUrl;
    private final int webhookTimeoutMs;

    public Config(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String configuredLanguage = config.getString("language", "ru");
        language = configuredLanguage != null && configuredLanguage.trim().equalsIgnoreCase("eng") ? "eng" : "ru";
        endpoint = config.getString("check.endpoint", "http://ip-api.com/json/{ip}?fields=status,message,proxy,hosting,as");
        timeoutMs = Math.max(250, config.getInt("check.timeout-ms", 2500));
        cacheSeconds = Math.max(0, config.getLong("check.cache-seconds", 300));
        cacheMaxEntries = Math.max(1, config.getInt("check.cache-max-entries", 4096));
        failOpen = config.getBoolean("check.fail-open", true);
        blockProxy = config.getBoolean("check.block-proxy", true);
        blockHosting = config.getBoolean("check.block-hosting", true);
        blockedAsns = parseAsns(config.getStringList("check.blocked-asns"));
        webhookEnabled = config.getBoolean("webhook.enabled", false);
        webhookUrl = config.getString("webhook.url", "");
        webhookTimeoutMs = Math.max(250, config.getInt("webhook.timeout-ms", 4000));
    }

    private Set<String> parseAsns(Iterable<String> values) {
        Set<String> result = new HashSet<>();
        for (String value : values) {
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("AS")) {
                normalized = normalized.substring(2);
            }
            if (normalized.matches("\\d+")) {
                result.add(normalized);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}