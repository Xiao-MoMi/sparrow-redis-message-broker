package net.momirealms.sparrow.redis.messagebroker.connection;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AbstractDefaultRedisConnection implements RedisConnection {
    public static final byte[] SELF_INCREASE_MESSAGE_ID = "sparrow:id".getBytes(StandardCharsets.UTF_8);
    protected final RedisClient redisClient;
    protected final StatefulRedisConnection<byte[], byte[]> publishConnection;
    protected final RedisAsyncCommands<byte[], byte[]> asyncPublishCmds;

    public AbstractDefaultRedisConnection(String redisUri, int queueSize) {
        this(createRedisClient(redisUri, queueSize));
    }

    protected static RedisClient createRedisClient(String redisUri, int queueSize) {
        return make(RedisClient.create(redisUri),
                client -> client.setOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .suspendReconnectOnProtocolFailure(false)
                        .requestQueueSize(queueSize)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.ACCEPT_COMMANDS).build())
        );
    }

    public AbstractDefaultRedisConnection(RedisClient redisClient) {
        this.redisClient = redisClient;
        this.publishConnection = this.redisClient.connect(new ByteArrayCodec());
        this.asyncPublishCmds = this.publishConnection.async();
    }

    protected void validateChannelAndMessage(byte[] channel, byte[] message) {
        Objects.requireNonNull(channel, "Channel cannot be null");
        if (message != null && channel.length == 0) {
            throw new IllegalArgumentException("Channel cannot be empty");
        }
    }

    @Override
    public boolean isOpen() {
        return this.publishConnection.isOpen();
    }

    @Override
    public CompletableFuture<Long> nextMessageId() {
        RedisFuture<Long> incr = this.asyncPublishCmds.incr(SELF_INCREASE_MESSAGE_ID);
        return incr.toCompletableFuture();
    }

    @Override
    public void close() {
        if (this.publishConnection != null && this.publishConnection.isOpen()) {
            this.publishConnection.close();
        }
    }

    private static <T> T make(T obj, Consumer<T> function) {
        function.accept(obj);
        return obj;
    }
}
