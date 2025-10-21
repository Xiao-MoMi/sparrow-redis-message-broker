package net.momirealms.sparrow.redis.messagebroker.plugin;

import net.momirealms.sparrow.redis.messagebroker.Logger;

import java.util.logging.Level;

public class JavaPluginLogger implements Logger {
    private final SparrowRedisMessageBrokerPlugin plugin;

    public JavaPluginLogger(SparrowRedisMessageBrokerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void error(String msg, Throwable t) {
        this.plugin.getLogger().log(Level.SEVERE, msg, t);
    }

    @Override
    public void warn(String msg, Throwable t) {
        this.plugin.getLogger().log(Level.WARNING, msg, t);
    }

    @Override
    public void info(String msg) {
        this.plugin.getLogger().log(Level.INFO, msg);
    }

    @Override
    public void debug(String msg) {
        if (this.plugin.devMode) {
            this.plugin.getLogger().log(Level.INFO, msg);
        }
    }
}
