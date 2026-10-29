package com.wiss.wiss.wantivpn;

import org.bukkit.ChatColor;

public final class Messages {
    private static final String TITLE =
            "\u00a7x\u00a70\u00a70\u00a7f\u00a7c\u00a7f\u00a7fw" +
            "\u00a7x\u00a70\u00a70\u00a7e\u00a76\u00a7f\u00a7fA" +
            "\u00a7x\u00a70\u00a70\u00a7d\u00a70\u00a7f\u00a7fn" +
            "\u00a7x\u00a70\u00a70\u00a7b\u00a7a\u00a7f\u00a7ft" +
            "\u00a7x\u00a70\u00a70\u00a7a\u00a75\u00a7f\u00a7fi" +
            "\u00a7x\u00a70\u00a70\u00a78\u00a7f\u00a7f\u00a7fV" +
            "\u00a7x\u00a70\u00a70\u00a77\u00a79\u00a7f\u00a7fP" +
            "\u00a7x\u00a70\u00a70\u00a76\u00a73\u00a7f\u00a7fN";

    private Messages() {
    }

    public static String vpnDenied(String language) {
        if ("eng".equals(language)) {
            return color(TITLE + "\n\n"
                    + "&fYou cannot connect\n"
                    + "&fto this server because you are\n"
                    + "&cusing a VPN or proxy.\n\n"
                    + "&fHow to fix it:\n"
                    + "&71. &fTurn off your VPN/proxy\n"
                    + "&72. &fCheck your internet connection\n"
                    + "&73. &fRestart your modem or router");
        }
        return color(TITLE + "\n\n"
                + "&fВы не можете\n"
                + "&fзайти на сервер, так как\n"
                + "&cиспользуете VPN или прокси.\n\n"
                + "&fКак исправить:\n"
                + "&71. &fВыключите VPN/прокси\n"
                + "&72. &fПроверьте стабильность своего интернета\n"
                + "&73. &fПерезагрузите модем или роутер");
    }

    public static String checkUnavailable(String language) {
        return color("eng".equals(language)
                ? "&cCould not verify your connection. Please try again later."
                : "&cНе удалось проверить подключение. Попробуйте зайти позже.");
    }

    public static String reloaded(String language) {
        return color(TITLE + ("eng".equals(language)
                ? " &7• &fPlugin reloaded"
                : " &7• &fПлагин перезагружен"));
    }

    public static String noPermission(String language) {
        return color("eng".equals(language)
                ? "&cYou do not have permission to use this command."
                : "&cУ вас нет прав для использования этой команды.");
    }

    public static String usage(String language) {
        return color("eng".equals(language)
                ? "&fUsage: &7/antivpn reload"
                : "&fИспользование: &7/antivpn reload");
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}