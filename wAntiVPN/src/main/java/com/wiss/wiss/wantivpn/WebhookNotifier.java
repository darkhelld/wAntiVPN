package com.wiss.wiss.wantivpn;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class WebhookNotifier {
    private final JavaPlugin plugin;
    private final Config config;

    public WebhookNotifier(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void sendAttempt(String playerName, String address, String network) {
        if (!config.isWebhookEnabled() || config.getWebhookUrl().trim().isEmpty()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> post(playerName, address, network, config.getLanguage()));
    }

    private void post(String playerName, String address, String network, String language) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(config.getWebhookUrl()).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.getWebhookTimeoutMs());
            connection.setReadTimeout(config.getWebhookTimeoutMs());
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            boolean english = "eng".equals(language);
            String payload = "{\"embeds\":[{\"title\":\""
                    + (english ? "VPN/Proxy connection attempt" : "Попытка входа через VPN/прокси")
                    + "\",\"color\":15158332,\"fields\":["
                    + field(english ? "Player" : "Игрок", playerName, true) + ","
                    + field("IP", address, true) + ","
                    + field(english ? "Network / ASN" : "Сеть / ASN", network, false)
                    + "]}]}";
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                plugin.getLogger().warning("Discord webhook вернул HTTP " + responseCode + ".");
            }
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().warning("Не удалось отправить уведомление в Discord webhook.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String field(String name, String value, boolean inline) {
        return "{\"name\":\"" + escape(name) + "\",\"value\":\"" + escape(value)
                + "\",\"inline\":" + inline + "}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}