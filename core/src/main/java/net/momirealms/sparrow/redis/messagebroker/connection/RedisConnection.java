package net.momirealms.sparrow.redis.messagebroker.connection;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface RedisConnection {

    void publish(byte[] channel, byte[] message);

    void unsubscribe(byte[] channel);

    void subscribe(byte[] channel, Consumer<byte[]> listener);

    void close();

    boolean isOpen();

    CompletableFuture<Long> nextMessageId();
}
