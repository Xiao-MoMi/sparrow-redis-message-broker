package net.momirealms.sparrow.redis.messagebroker;

import net.momirealms.sparrow.redis.messagebroker.connection.RedisConnection;
import net.momirealms.sparrow.redis.messagebroker.registry.DefaultMessageRegistry;
import net.momirealms.sparrow.redis.messagebroker.registry.RedisMessageRegistry;

import java.util.Objects;
import java.util.function.IntFunction;

public interface MessageBroker {

    byte[] channel();

    RedisMessageRegistry registry();

    void publish(RedisMessage message);

    static Builder builder() {
        return new Builder();
    }

    void unsubscribe();

    void subscribe();

    RedisConnection connection();

    class Builder {
        private IntFunction<RedisMessageRegistry> registryFunction = DefaultMessageRegistry::new;
        private byte[] channel;
        private int expectedSize = 16;
        private RedisConnection connection;

        private Builder() {}

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

        public MessageBroker build() {
            Objects.requireNonNull(this.channel, "channel");
            Objects.requireNonNull(this.registryFunction, "registryFunction");
            Objects.requireNonNull(this.connection, "connection");
            return new MessageBrokerImpl(this.channel, this.registryFunction.apply(this.expectedSize), this.connection);
        }
    }
}
