package net.momirealms.sparrow.redis.messagebroker.plugin;

import net.momirealms.sparrow.redis.messagebroker.Logger;
import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.connection.PubSubRedisConnection;
import net.momirealms.sparrow.redis.messagebroker.plugin.benchmark.PubSubBenchmarkConfig;
import net.momirealms.sparrow.redis.messagebroker.plugin.benchmark.RedisPubSubBenchmark;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.HelloMessage;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.PlayerInfoMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
        this.messageBroker.registry().register(PlayerInfoMessage.ID, PlayerInfoMessage.CODEC);
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
        JavaPluginLogger logger = new JavaPluginLogger(this);
        this.logBenchMark(logger, "Hello World");
        new RedisPubSubBenchmark(this.messageBroker, logger).runBenchmark(PubSubBenchmarkConfig.builder()
                .message(new HelloMessage("Hello World!"))
                .totalMessages(100_000)
                .warmupMessages(30_000)
                .build());
        this.logBenchMark(logger, "Player Info");
        new RedisPubSubBenchmark(this.messageBroker, logger).runBenchmark(PubSubBenchmarkConfig.builder()
                .message(new PlayerInfoMessage("XiaoMoMi", UUID.randomUUID(), "survival_world",
                        getRandomDouble(), getRandomDouble(), getRandomDouble(), getRandomFloat(), getRandomFloat()))
                .totalMessages(100_000)
                .warmupMessages(0)
                .build());
    }

    private void logBenchMark(Logger logger, String message) {
        logger.info("");
        logger.info("");
        logger.info("=========  BenchMark (" + message + ") =========");
    }

    private double getRandomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    private float getRandomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }
}
