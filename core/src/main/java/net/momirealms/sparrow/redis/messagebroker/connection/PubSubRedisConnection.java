package net.momirealms.sparrow.redis.messagebroker.connection;

import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.momirealms.sparrow.redis.messagebroker.Logger;
import net.momirealms.sparrow.redis.messagebroker.RedisMessageExecutionException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PubSubRedisConnection extends AbstractDefaultRedisConnection {
    private final StatefulRedisPubSubConnection<byte[], byte[]> subscribeConnection;
    private final RedisPubSubCommands<byte[], byte[]> syncSubscribeCmds;
    private final Map<String, Consumer<byte[]>> channelListeners;

    public PubSubRedisConnection(String redisUri, Logger logger) {
        super(redisUri, logger);
        this.subscribeConnection = super.redisClient.connectPubSub(new ByteArrayCodec());
        this.syncSubscribeCmds = this.subscribeConnection.sync();
        this.channelListeners = new ConcurrentHashMap<>();

        // 设置消息监听器
        setupMessageListener();
    }

    @Override
    public boolean isOpen() {
        return this.subscribeConnection.isOpen() && super.isOpen();
    }

    @Override
    public void publish(byte[] channel, byte[] message) {
        super.validateChannelAndMessage(channel, message);
        super.asyncPublishCmds.publish(channel, message);
    }

    @Override
    public void unsubscribe(byte[] channel) {
        if (channel == null) {
            return;
        }

        String channelKey = new String(channel, StandardCharsets.UTF_8);
        Consumer<byte[]> removed = this.channelListeners.remove(channelKey);

        if (removed != null) {
            // 取消订阅
            this.syncSubscribeCmds.unsubscribe(channel);
        }
    }

    @Override
    public void subscribe(byte[] channel, Consumer<byte[]> listener) {
        if (channel == null || listener == null) {
            throw new IllegalArgumentException("Channel and listener cannot be null");
        }

        String channelKey = new String(channel, StandardCharsets.UTF_8);
        Consumer<byte[]> previous = this.channelListeners.put(channelKey, listener);

        if (previous == null) {
            // 订阅频道
            this.syncSubscribeCmds.subscribe(channel);
        }
    }

    @Override
    public void close() {
        try {
            if (this.subscribeConnection != null) {
                this.subscribeConnection.close();
            }
            if (this.redisClient != null) {
                this.redisClient.close();
            }
            this.channelListeners.clear();
            super.close();
            this.logger.info("Redis PubSub connection closed successfully");
        } catch (Exception e) {
            this.logger.error("Error closing Redis PubSub connection", e);
        }
    }

    public int getActiveChannelCount() {
        return this.channelListeners.size();
    }

    public boolean isListening(byte[] channel) {
        if (channel == null) return false;
        String channelKey = new String(channel, StandardCharsets.UTF_8);
        return this.channelListeners.containsKey(channelKey);
    }

    private void setupMessageListener() {
        this.subscribeConnection.addListener(new RedisPubSubListener<>() {
            @Override
            public void message(byte[] channel, byte[] message) {
                String channelKey = new String(channel, StandardCharsets.UTF_8);
                Consumer<byte[]> listener = PubSubRedisConnection.this.channelListeners.get(channelKey);
                if (listener != null) {
                    try {
                        listener.accept(message);
                    } catch (RedisMessageExecutionException e) {
                        PubSubRedisConnection.this.logger.error("Error processing channel message", e.getCause());
                    } catch (Exception e) {
                        PubSubRedisConnection.this.logger.error("Error processing channel message", e);
                    }
                }
            }

            @Override
            public void message(byte[] pattern, byte[] channel, byte[] message) {
            }

            @Override
            public void subscribed(byte[] channel, long count) {
            }

            @Override
            public void unsubscribed(byte[] channel, long count) {
            }

            @Override
            public void psubscribed(byte[] pattern, long count) {
            }

            @Override
            public void punsubscribed(byte[] pattern, long count) {
            }
        });
    }
}
