package net.momirealms.sparrow.redis.messagebroker.plugin.benchmark;

public class PubSubBenchmarkConfig {
    private final int messageSize;
    private final int totalMessages;
    private final int warmupMessages;
    private final String testChannel;

    private PubSubBenchmarkConfig(Builder builder) {
        this.messageSize = builder.messageSize;
        this.totalMessages = builder.totalMessages;
        this.warmupMessages = builder.warmupMessages;
        this.testChannel = builder.testChannel;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public int getWarmupMessages() {
        return warmupMessages;
    }

    public String getTestChannel() {
        return testChannel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int messageSize = 128; // 默认100字节
        private int totalMessages = 10000;
        private int warmupMessages = 1000;
        private String testChannel = "sparrow:benchmark";

        public Builder messageSize(int messageSize) {
            this.messageSize = messageSize;
            return this;
        }

        public Builder totalMessages(int totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public Builder warmupMessages(int warmupMessages) {
            this.warmupMessages = warmupMessages;
            return this;
        }

        public Builder testChannel(String testChannel) {
            this.testChannel = testChannel;
            return this;
        }

        public PubSubBenchmarkConfig build() {
            return new PubSubBenchmarkConfig(this);
        }
    }
}