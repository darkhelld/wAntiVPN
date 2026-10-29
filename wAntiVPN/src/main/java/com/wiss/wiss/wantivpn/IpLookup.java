package com.wiss.wiss.wantivpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IpLookup {
    private static final Pattern STATUS_PATTERN = Pattern.compile("\"status\"\\s*:\\s*\"success\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROXY_PATTERN = Pattern.compile("\"proxy\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOSTING_PATTERN = Pattern.compile("\"hosting\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NETWORK_PATTERN = Pattern.compile("\"as\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern ASN_PATTERN = Pattern.compile("AS(\\d+)", Pattern.CASE_INSENSITIVE);

    private final Config config;
    private final Map<String, CachedResult> cache = new LinkedHashMap<>(16, 0.75f, true);

    public IpLookup(Config config) {
        this.config = config;
    }

    public CheckResult check(String address) {
        CheckResult cached = getCached(address);
        if (cached != null) {
            return cached;
        }

        CheckResult result;
        try {
            result = lookup(address);
        } catch (IOException | RuntimeException exception) {
            result = CheckResult.unavailable();
        }
        cache(address, result);
        return result;
    }

    private CheckResult lookup(String address) throws IOException {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.name());
        URL url = new URL(config.getEndpoint().replace("{ip}", encodedAddress));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(config.getTimeoutMs());
        connection.setReadTimeout(config.getTimeoutMs());
        connection.setUseCaches(false);

        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return CheckResult.unavailable();
            }

            String response = readResponse(connection);
            if (!STATUS_PATTERN.matcher(response).find()) {
                return CheckResult.unavailable();
            }

            Boolean proxy = readBoolean(PROXY_PATTERN, response);
            Boolean hosting = readBoolean(HOSTING_PATTERN, response);
            Matcher networkMatcher = NETWORK_PATTERN.matcher(response);
            if (proxy == null || hosting == null || !networkMatcher.find()) {
                return CheckResult.unavailable();
            }

            String network = networkMatcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
            boolean blocked = (config.isBlockProxy() && proxy)
                    || (config.isBlockHosting() && hosting)
                    || isBlockedAsn(network);
            return new CheckResult(true, blocked, network.isEmpty() ? "Не определена" : network);
        } finally {
            connection.disconnect();
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                if (response.length() > 32768) {
                    throw new IOException("Lookup response is too large");
                }
            }
        }
        return response.toString();
    }

    private Boolean readBoolean(Pattern pattern, String response) {
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? Boolean.valueOf(matcher.group(1)) : null;
    }

    private boolean isBlockedAsn(String network) {
        Matcher matcher = ASN_PATTERN.matcher(network);
        return matcher.find() && config.getBlockedAsns().contains(matcher.group(1));
    }

    private synchronized CheckResult getCached(String address) {
        CachedResult cached = cache.get(address);
        if (cached == null) {
            return null;
        }
        if (cached.expiresAt <= System.currentTimeMillis()) {
            cache.remove(address);
            return null;
        }
        return cached.result;
    }

    private synchronized void cache(String address, CheckResult result) {
        long ttlSeconds = config.getCacheSeconds();
        if (ttlSeconds <= 0) {
            return;
        }
        if (!result.isAvailable()) {
            ttlSeconds = Math.min(ttlSeconds, 5);
        }
        cache.put(address, new CachedResult(result, System.currentTimeMillis() + ttlSeconds * 1000L));
        while (cache.size() > config.getCacheMaxEntries()) {
            Iterator<String> iterator = cache.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    private static final class CachedResult {
        private final CheckResult result;
        private final long expiresAt;

        private CachedResult(CheckResult result, long expiresAt) {
            this.result = result;
            this.expiresAt = expiresAt;
        }
    }
}