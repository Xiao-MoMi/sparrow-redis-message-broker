package net.momirealms.sparrow.redis.messagebroker.connection;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import net.momirealms.sparrow.redis.messagebroker.Logger;

import java.util.Objects;

public abstract class AbstractDefaultRedisConnection implements RedisConnection {
    protected final Logger logger;
    protected final RedisClient redisClient;
    protected final StatefulRedisConnection<byte[], byte[]> publishConnection;
    protected final RedisAsyncCommands<byte[], byte[]> asyncPublishCmds;

    public AbstractDefaultRedisConnection(String redisUri, Logger logger) {
        this.redisClient = RedisClient.create(redisUri);
        ClientOptions options = ClientOptions.builder()
                .autoReconnect(true)
                .suspendReconnectOnProtocolFailure(false)
                .requestQueueSize(1_000)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.ACCEPT_COMMANDS)
                .build();
        this.redisClient.setOptions(options);
        this.publishConnection = this.redisClient.connect(new ByteArrayCodec());
        this.asyncPublishCmds = this.publishConnection.async();
        this.logger = logger;
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
    public void close() {
        this.publishConnection.close();
        this.redisClient.shutdown();
    }
}
