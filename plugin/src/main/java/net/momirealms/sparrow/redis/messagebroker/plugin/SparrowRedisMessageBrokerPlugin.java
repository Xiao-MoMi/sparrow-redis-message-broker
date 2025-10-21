package net.momirealms.sparrow.redis.messagebroker.plugin;

import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.connection.PubSubRedisConnection;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.HelloMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class SparrowRedisMessageBrokerPlugin extends JavaPlugin {
    private MessageBroker messageBroker;
    protected boolean devMode;

    @Override
    public void onEnable() {
        if ("true".equals(PluginProperties.getValue("dev_mode"))) {
            this.devMode = true;
            YamlConfiguration yaml = getOrSaveConfig();
            String redisUri = yaml.getString("uri");
            this.messageBroker = MessageBroker.builder()
                    .channel("sparrow:test".getBytes(StandardCharsets.UTF_8))
                    .connection(new PubSubRedisConnection(redisUri, new JavaPluginLogger(this)))
                    .build();
            this.messageBroker.registry().register(HelloMessage.ID, HelloMessage.CODEC);
            this.messageBroker.subscribe();
            this.getLogger().info("Connected to Redis");
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> this.messageBroker.publish(new HelloMessage("Hello World!")), 10L, 10L);
        }
    }

    @Override
    public void onDisable() {
        if (this.messageBroker != null) {
            this.messageBroker.unsubscribe();
            this.messageBroker.connection().close();
        }
    }

    private YamlConfiguration getOrSaveConfig() {
        File configFile = new File(getDataFolder(), "redis.yml");
        if (!configFile.exists()) {
            this.saveResource("redis.yml", false);
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }
}
