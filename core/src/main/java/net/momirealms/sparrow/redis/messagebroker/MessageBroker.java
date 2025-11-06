package net.momirealms.sparrow.redis.messagebroker;

import net.momirealms.sparrow.redis.messagebroker.connection.RedisConnection;
import net.momirealms.sparrow.redis.messagebroker.message.OneWayMessage;
import net.momirealms.sparrow.redis.messagebroker.message.TwoWayRequestMessage;
import net.momirealms.sparrow.redis.messagebroker.message.TwoWayResponseMessage;
import net.momirealms.sparrow.redis.messagebroker.registry.DefaultMessageRegistry;
import net.momirealms.sparrow.redis.messagebroker.registry.RedisMessageRegistry;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

public interface MessageBroker {

    String serverId();

    Set<String> tags();

    byte[] channel();

    RedisMessageRegistry registry();

    void publish(RedisMessage message);

    void publishOneWay(OneWayMessage message, String targetServer);

    <R extends TwoWayResponseMessage> CompletableFuture<R> publishTwoWay(TwoWayRequestMessage<R> message, String targetServer);

    void response(TwoWayResponseMessage message);

    static Builder builder() {
        return new Builder();
    }

    byte[] encode(RedisMessage message);

    RedisMessage decode(byte[] bytes);

    void unsubscribe();

    void subscribe();

    RedisConnection connection();

    class Builder {
        private IntFunction<RedisMessageRegistry> registryFunction = DefaultMessageRegistry::new;
        private byte[] channel;
        private int expectedSize = 16;
        private RedisConnection connection;
        private String serverId;
        private Set<String> tags = Collections.emptySet();
        private Logger logger;

        private Builder() {}

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder connection(RedisConnection connection) {
            this.connection = connection;
            return this;
        }

        public Builder channel(final byte[] channel) {
            this.channel = channel;
            return this;
        }

        public Builder registry(final IntFunction<RedisMessageRegistry> function) {
            this.registryFunction = function;
            return this;
        }

        public Builder expectedSize(final int expectedSize) {
            this.expectedSize = Math.max(expectedSize, 16);
            return this;
        }

        public Builder serverId(final String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder tags(final Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public MessageBroker build() {
            Objects.requireNonNull(this.logger, "logger cannot be null");
            Objects.requireNonNull(this.channel, "channel");
            Objects.requireNonNull(this.registryFunction, "registryFunction");
            Objects.requireNonNull(this.connection, "connection");
            Objects.requireNonNull(this.serverId, "serverId");
            return new MessageBrokerImpl(this.logger, this.channel, this.registryFunction.apply(this.expectedSize), this.connection, this.serverId, this.tags);
        }
    }
}
