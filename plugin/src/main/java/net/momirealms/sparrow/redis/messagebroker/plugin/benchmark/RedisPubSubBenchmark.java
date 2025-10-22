package net.momirealms.sparrow.redis.messagebroker.plugin.benchmark;

import net.momirealms.sparrow.redis.messagebroker.Logger;
import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.connection.RedisConnection;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RedisPubSubBenchmark {
    private final RedisConnection connection;
    private final MessageBroker broker;
    private final Logger logger;

    public RedisPubSubBenchmark(MessageBroker broker, Logger logger) {
        this.connection = broker.connection();
        this.broker = broker;
        this.logger = logger;
    }

    public void runBenchmark(PubSubBenchmarkConfig config) {
        this.logger.info("Starting Redis PubSub benchmark (Single Thread)...");

        byte[] message = this.broker.encode(config.getMessage());
        this.logger.info("Configuration: " +
                config.getTotalMessages() + " messages, " +
                message.length + " bytes each");

        // 预热
        this.warmup(config, message);

        // 执行基准测试
        this.executeBenchmarkSingleThread(config);
    }

    private void warmup(PubSubBenchmarkConfig config, byte[] messageBytes) {
        if (config.getWarmupMessages() <= 0) return;

        this.logger.info("Warming up with " + config.getWarmupMessages() + " messages...");

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
                this.connection.publish(channel, messageBytes);
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
        byte[] channel = config.getTestChannel().getBytes(StandardCharsets.UTF_8);

        // 用于统计的原子变量
        AtomicInteger messagesReceived = new AtomicInteger(0);
        AtomicLong totalEncodeTime = new AtomicLong(0);
        AtomicLong maxEncodeTime = new AtomicLong(0);
        AtomicLong minEncodeTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong totalPublishTime = new AtomicLong(0);
        AtomicLong maxPublishTime = new AtomicLong(0);
        AtomicLong minPublishTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong totalDecodeTime = new AtomicLong(0);
        AtomicLong maxDecodeTime = new AtomicLong(0);
        AtomicLong minDecodeTime = new AtomicLong(Long.MAX_VALUE);

        CountDownLatch completionLatch = new CountDownLatch(config.getTotalMessages());

        this.logger.info("Starting subscriber...");

        // 启动订阅者
        this.connection.subscribe(channel, message -> {
            // 解码并统计耗时
            long decodeStartTime = System.nanoTime();
            this.broker.decode(message);
            long decodeTime = System.nanoTime() - decodeStartTime;

            // 更新解码统计
            totalDecodeTime.addAndGet(decodeTime);
            maxDecodeTime.set(Math.max(maxDecodeTime.get(), decodeTime));
            minDecodeTime.set(Math.min(minDecodeTime.get(), decodeTime));

            messagesReceived.incrementAndGet();
            completionLatch.countDown();

            // 进度显示
            if (messagesReceived.get() % 20000 == 0) {
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
                // 编码统计
                long encodeStartTime = System.nanoTime();
                byte[] encodedMessage = this.broker.encode(config.getMessage());
                long encodeTime = System.nanoTime() - encodeStartTime;

                totalEncodeTime.addAndGet(encodeTime);
                maxEncodeTime.set(Math.max(maxEncodeTime.get(), encodeTime));
                minEncodeTime.set(Math.min(minEncodeTime.get(), encodeTime));

                // 发布统计
                long publishStartTime = System.nanoTime();
                this.connection.publish(channel, encodedMessage);
                long publishTime = System.nanoTime() - publishStartTime;

                totalPublishTime.addAndGet(publishTime);
                maxPublishTime.set(Math.max(maxPublishTime.get(), publishTime));
                minPublishTime.set(Math.min(minPublishTime.get(), publishTime));

                // 进度显示
                if ((i + 1) % 20000 == 0) {
                    this.logger.info("Progress: " + (i + 1) + "/" + config.getTotalMessages() + " messages sent");
                }
            }

            this.logger.info("All messages published, waiting for reception...");

            // 等待所有消息完成
            if (!completionLatch.await(30, TimeUnit.SECONDS)) {
                this.logger.info("Benchmark timed out, received " + messagesReceived.get() + "/" + config.getTotalMessages() + " messages");
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            // 计算统计信息
            long totalMessages = config.getTotalMessages();
            long actualReceived = messagesReceived.get();

            // 编码耗时统计
            long avgEncodeTimeNs = totalEncodeTime.get() / totalMessages;
            long avgEncodeTimeMs = avgEncodeTimeNs / 1_000_000;
            long maxEncodeTimeMs = maxEncodeTime.get() / 1_000_000;
            long minEncodeTimeMs = minEncodeTime.get() == Long.MAX_VALUE ? 0 : minEncodeTime.get() / 1_000_000;

            // 发布耗时统计
            long avgPublishTimeNs = totalPublishTime.get() / totalMessages;
            long avgPublishTimeMs = avgPublishTimeNs / 1_000_000;
            long maxPublishTimeMs = maxPublishTime.get() / 1_000_000;
            long minPublishTimeMs = minPublishTime.get() == Long.MAX_VALUE ? 0 : minPublishTime.get() / 1_000_000;

            // 解码耗时统计
            long avgDecodeTimeNs = actualReceived > 0 ? totalDecodeTime.get() / actualReceived : 0;
            long avgDecodeTimeMs = avgDecodeTimeNs / 1_000_000;
            long maxDecodeTimeMs = maxDecodeTime.get() / 1_000_000;
            long minDecodeTimeMs = minDecodeTime.get() == Long.MAX_VALUE ? 0 : minDecodeTime.get() / 1_000_000;

            // 计算结果
            long messagesPerSecond = (actualReceived * 1000L) / Math.max(1, totalTime);

            this.logger.info("Benchmark completed:");
            this.logger.info("  Total time: " + totalTime + " ms");
            this.logger.info("  Messages sent: " + config.getTotalMessages());
            this.logger.info("  Messages received: " + actualReceived);
            this.logger.info("  Throughput: " + messagesPerSecond + " msg/sec");
            this.logger.info("  Encode time statistics:");
            this.logger.info("    Average: " + avgEncodeTimeMs + " ms (" + avgEncodeTimeNs + " ns)");
            this.logger.info("    Max: " + maxEncodeTimeMs + " ms (" + maxEncodeTime.get() + " ns)");
            this.logger.info("    Min: " + minEncodeTimeMs + " ms (" + (minEncodeTime.get() == Long.MAX_VALUE ? 0 : minEncodeTime.get()) + " ns)");
            this.logger.info("    Total encode time: " + (totalEncodeTime.get() / 1_000_000) + " ms");
            this.logger.info("  Publish time statistics:");
            this.logger.info("    Average: " + avgPublishTimeMs + " ms (" + avgPublishTimeNs + " ns)");
            this.logger.info("    Max: " + maxPublishTimeMs + " ms (" + maxPublishTime.get() + " ns)");
            this.logger.info("    Min: " + minPublishTimeMs + " ms (" + (minPublishTime.get() == Long.MAX_VALUE ? 0 : minPublishTime.get()) + " ns)");
            this.logger.info("    Total publish time: " + (totalPublishTime.get() / 1_000_000) + " ms");
            this.logger.info("  Decode time statistics:");
            this.logger.info("    Average: " + avgDecodeTimeMs + " ms (" + avgDecodeTimeNs + " ns)");
            this.logger.info("    Max: " + maxDecodeTimeMs + " ms (" + maxDecodeTime.get() + " ns)");
            this.logger.info("    Min: " + minDecodeTimeMs + " ms (" + (minDecodeTime.get() == Long.MAX_VALUE ? 0 : minDecodeTime.get()) + " ns)");
            this.logger.info("    Total decode time: " + (totalDecodeTime.get() / 1_000_000) + " ms");

            // 总处理时间分析
            long totalProcessingTimeMs = (totalEncodeTime.get() + totalPublishTime.get() + totalDecodeTime.get()) / 1_000_000;
            this.logger.info("  Total processing time (encode + publish + decode): " + totalProcessingTimeMs + " ms");

        } catch (Exception e) {
            this.logger.error("Benchmark execution failed", e);
            throw new RuntimeException("Benchmark execution failed", e);
        } finally {
            // 清理
            this.connection.unsubscribe(channel);
        }
    }
}