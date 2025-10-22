package net.momirealms.sparrow.redis.messagebroker.plugin.benchmark;

import net.momirealms.sparrow.redis.messagebroker.Logger;
import net.momirealms.sparrow.redis.messagebroker.connection.RedisConnection;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisPubSubBenchmark {
    private final RedisConnection connection;
    private final Logger logger;

    public RedisPubSubBenchmark(RedisConnection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public void runBenchmark(PubSubBenchmarkConfig config) {
        this.logger.info("Starting Redis PubSub benchmark (Single Thread)...");
        this.logger.info("Configuration: " +
                config.getTotalMessages() + " messages, " +
                config.getMessageSize() + " bytes each");

        // 预热
        this.warmup(config);

        // 执行基准测试
        this.executeBenchmarkSingleThread(config);
    }

    private void warmup(PubSubBenchmarkConfig config) {
        if (config.getWarmupMessages() <= 0) return;

        this.logger.info("Warming up with " + config.getWarmupMessages() + " messages...");
        byte[] warmupMessage = generateMessage(config.getMessageSize());
        byte[] channel = config.getTestChannel().getBytes(StandardCharsets.UTF_8);

        CountDownLatch warmupLatch = new CountDownLatch(config.getWarmupMessages());
        AtomicInteger received = new AtomicInteger(0);

        // 订阅
        this.connection.subscribe(channel, message -> {
            received.incrementAndGet();
            warmupLatch.countDown();
        });

        try {
            // 发布预热消息
            for (int i = 0; i < config.getWarmupMessages(); i++) {
                this.connection.publish(channel, warmupMessage);
            }

            // 等待所有消息被接收
            if (!warmupLatch.await(10, TimeUnit.SECONDS)) {
                this.logger.info("Warmup timed out, received " + received.get() + "/" + config.getWarmupMessages() + " messages");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 取消订阅
            this.connection.unsubscribe(channel);
        }

        this.logger.info("Warmup completed, received " + received.get() + " messages");
    }

    private void executeBenchmarkSingleThread(PubSubBenchmarkConfig config) {
        byte[] testMessage = generateMessage(config.getMessageSize());
        byte[] channel = config.getTestChannel().getBytes(StandardCharsets.UTF_8);

        // 用于统计的原子变量
        AtomicInteger messagesReceived = new AtomicInteger(0);

        CountDownLatch completionLatch = new CountDownLatch(config.getTotalMessages());

        this.logger.info("Starting subscriber...");

        // 启动订阅者（在同一个线程中）
        this.connection.subscribe(channel, message -> {
            messagesReceived.incrementAndGet();
            completionLatch.countDown();

            // 进度显示
            if (messagesReceived.get() % 10000 == 0) {
                this.logger.info("Progress: " + messagesReceived.get() + "/" + config.getTotalMessages() + " messages received");
            }
        });

        try {
            // 等待订阅生效
            Thread.sleep(1000);

            this.logger.info("Subscriber ready, starting publisher...");
            long startTime = System.currentTimeMillis();

            // 发布消息（在同一个线程中顺序执行）
            for (int i = 0; i < config.getTotalMessages(); i++) {
                this.connection.publish(channel, testMessage);

                // 进度显示
                if ((i + 1) % 10000 == 0) {
                    this.logger.info("Progress: " + (i + 1) + "/" + config.getTotalMessages() + " messages sent");
                }

                // 可选：添加小延迟避免过载
                // if ((i + 1) % 1000 == 0) {
                //     Thread.sleep(1);
                // }
            }

            this.logger.info("All messages published, waiting for reception...");

            // 等待所有消息完成
            if (!completionLatch.await(30, TimeUnit.SECONDS)) {
                this.logger.info("Benchmark timed out, received " + messagesReceived.get() + "/" + config.getTotalMessages() + " messages");
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            // 计算结果
            long actualReceived = messagesReceived.get();
            long messagesPerSecond = (actualReceived * 1000L) / Math.max(1, totalTime);

            this.logger.info("Benchmark completed:");
            this.logger.info("  Total time: " + totalTime + " ms");
            this.logger.info("  Messages sent: " + config.getTotalMessages());
            this.logger.info("  Messages received: " + actualReceived);
            this.logger.info("  Throughput: " + messagesPerSecond + " msg/sec");
        } catch (Exception e) {
            this.logger.error("Benchmark execution failed", e);
            throw new RuntimeException("Benchmark execution failed", e);
        } finally {
            // 清理
            this.connection.unsubscribe(channel);
        }
    }

    private byte[] generateMessage(int size) {
        byte[] message = new byte[size];
        // 填充一些测试数据
        for (int i = 0; i < message.length; i++) {
            message[i] = (byte) ('A' + (i % 26));
        }
        return message;
    }
}