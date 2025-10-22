package net.momirealms.sparrow.redis.messagebroker.plugin;

import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.connection.PubSubRedisConnection;
import net.momirealms.sparrow.redis.messagebroker.plugin.benchmark.PubSubBenchmarkConfig;
import net.momirealms.sparrow.redis.messagebroker.plugin.benchmark.RedisPubSubBenchmark;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.HelloMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class SparrowRedisMessageBrokerPlugin extends JavaPlugin {
    private static SparrowRedisMessageBrokerPlugin instance;
    private final SparrowRedisMessageBrokerBootstrap bootstrap;
    private MessageBroker messageBroker;

    public SparrowRedisMessageBrokerPlugin(SparrowRedisMessageBrokerBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        instance = this;
    }

    public static SparrowRedisMessageBrokerPlugin instance() {
        return instance;
    }

    public SparrowRedisMessageBrokerBootstrap bootstrap() {
        return bootstrap;
    }

    @Override
    public void onEnable() {
        YamlConfiguration yaml = getOrSaveConfig();
        String redisUri = yaml.getString("uri");
        this.messageBroker = MessageBroker.builder()
                .channel("sparrow:test".getBytes(StandardCharsets.UTF_8))
                .connection(new PubSubRedisConnection(redisUri, new JavaPluginLogger(this)))
                .build();
        this.messageBroker.registry().register(HelloMessage.ID, HelloMessage.CODEC);
        this.messageBroker.subscribe();
        this.getLogger().info("Connected to Redis");
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

    public void startBenchMark() {
        PubSubBenchmarkConfig config = PubSubBenchmarkConfig.builder()
                .message(new HelloMessage("Hello World"))
                .totalMessages(100_000)
                .warmupMessages(10_000)
                .build();
        RedisPubSubBenchmark benchmark = new RedisPubSubBenchmark(this.messageBroker, new JavaPluginLogger(this));
        benchmark.runBenchmark(config);
    }
}
