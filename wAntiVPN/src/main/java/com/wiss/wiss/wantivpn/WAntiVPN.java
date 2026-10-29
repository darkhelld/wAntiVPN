package com.wiss.wiss.wantivpn;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class WAntiVPN extends JavaPlugin implements Listener, CommandExecutor {
    private volatile RuntimeState runtime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runtime = new RuntimeState(this);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("antivpn").setExecutor(this);
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String address = event.getAddress().getHostAddress();
        RuntimeState activeRuntime = runtime;
        CheckResult result = activeRuntime.ipLookup.check(address);

        if (!result.isAvailable()) {
            if (!activeRuntime.config.isFailOpen()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Messages.checkUnavailable(activeRuntime.config.getLanguage()));
            }
            return;
        }

        if (result.isBlocked()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Messages.vpnDenied(activeRuntime.config.getLanguage()));
            activeRuntime.webhookNotifier.sendAttempt(event.getName(), address, result.getNetwork());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String language = runtime.config.getLanguage();
        if (!sender.hasPermission("wantivpn.reload")) {
            sender.sendMessage(Messages.noPermission(language));
            return true;
        }
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Messages.usage(language));
            return true;
        }

        reloadConfig();
        runtime = new RuntimeState(this);
        sender.sendMessage(Messages.reloaded(runtime.config.getLanguage()));
        return true;
    }

    private static final class RuntimeState {
        private final Config config;
        private final IpLookup ipLookup;
        private final WebhookNotifier webhookNotifier;

        private RuntimeState(WAntiVPN plugin) {
            config = new Config(plugin);
            ipLookup = new IpLookup(config);
            webhookNotifier = new WebhookNotifier(plugin, config);
        }
    }
}