package net.momirealms.sparrow.redis.messagebroker.plugin;

import net.momirealms.sparrow.redis.messagebroker.Logger;
import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.connection.PubSubRedisConnection;
import net.momirealms.sparrow.redis.messagebroker.plugin.benchmark.PubSubBenchmarkConfig;
import net.momirealms.sparrow.redis.messagebroker.plugin.benchmark.RedisPubSubBenchmark;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.HelloMessage;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.PlayerCountRequestMessage;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.PlayerCountResponseMessage;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.PlayerInfoMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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
        String redisUri = yaml.getString("redis-uri");
        JavaPluginLogger logger = new JavaPluginLogger(this);
        this.messageBroker = MessageBroker.builder()
                .logger(logger)
                .serverId(yaml.getString("server-id", "survival_1"))
                .tags(new HashSet<>(yaml.getStringList("server-tags")))
                .channel("sparrow:test".getBytes(StandardCharsets.UTF_8))
                .connection(new PubSubRedisConnection(redisUri, 100_000, logger))
                .build();
        this.messageBroker.registry().register(HelloMessage.ID, HelloMessage.CODEC);
        this.messageBroker.registry().register(PlayerInfoMessage.ID, PlayerInfoMessage.CODEC);
        this.messageBroker.registry().register(PlayerCountRequestMessage.ID, PlayerCountRequestMessage.CODEC);
        this.messageBroker.registry().register(PlayerCountResponseMessage.ID, PlayerCountResponseMessage.CODEC);
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
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public void startBenchMark() {
        JavaPluginLogger logger = new JavaPluginLogger(this);
        this.logBenchMark(logger, "Hello World");
        new RedisPubSubBenchmark(this.messageBroker, logger).runBenchmark(PubSubBenchmarkConfig.builder()
                .message(new HelloMessage("Hello World!"))
                .totalMessages(100_000)
                .warmupMessages(100_000)
                .build());
        this.logBenchMark(logger, "Player Info");
        new RedisPubSubBenchmark(this.messageBroker, logger).runBenchmark(PubSubBenchmarkConfig.builder()
                .message(new PlayerInfoMessage("XiaoMoMi", UUID.randomUUID(), "survival_world",
                        getRandomDouble(), getRandomDouble(), getRandomDouble(), getRandomFloat(), getRandomFloat()))
                .totalMessages(100_000)
                .warmupMessages(0)
                .build());
    }

    public MessageBroker messageBroker() {
        return messageBroker;
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
