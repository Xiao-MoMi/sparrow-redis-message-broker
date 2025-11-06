package net.momirealms.sparrow.redis.messagebroker;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import net.momirealms.sparrow.redis.messagebroker.connection.RedisConnection;
import net.momirealms.sparrow.redis.messagebroker.message.OneWayMessage;
import net.momirealms.sparrow.redis.messagebroker.message.TwoWayRequestMessage;
import net.momirealms.sparrow.redis.messagebroker.message.TwoWayResponseMessage;
import net.momirealms.sparrow.redis.messagebroker.registry.RedisMessageRegistry;
import net.momirealms.sparrow.redis.messagebroker.registry.RegisteredRedisMessage;
import net.momirealms.sparrow.redis.messagebroker.util.ByteBufHelper;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class MessageBrokerImpl implements MessageBroker {
    private final Logger logger;
    private final ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
    private final byte[] channel;
    private final RedisMessageRegistry registry;
    private final RedisConnection connection;
    private final String serverId;
    private final Set<String> tags;
    private final Cache<Long, TimeStampedFuture<TwoWayResponseMessage>> pendingResponses =
            Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .initialCapacity(64)
            .evictionListener((Long key, TimeStampedFuture<TwoWayResponseMessage> value, RemovalCause cause) -> {
                if (value == null) return;
                if (cause == RemovalCause.EXPIRED) {
                    long time = value.time();
                    Instant instant = Instant.ofEpochMilli(time);
                    String formattedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            .withZone(ZoneId.systemDefault())
                            .format(instant);
                    value.future().completeExceptionally(new TimeoutException("Request sent at " + formattedTime + " has been expired"));
                }
            })
            .scheduler(Scheduler.systemScheduler())
            .build();

    public MessageBrokerImpl(Logger logger, byte[] channel, RedisMessageRegistry registry, RedisConnection connection, String serverId, Set<String> tags) {
        this.logger = logger;
        this.channel = channel;
        this.registry = registry;
        this.connection = connection;
        this.serverId = serverId;
        this.tags = tags;
    }

    @Override
    public Set<String> tags() {
        return this.tags;
    }

    @Override
    public String serverId() {
        return this.serverId;
    }

    @Override
    public byte[] channel() {
        return this.channel;
    }

    @Override
    public RedisMessageRegistry registry() {
        return registry;
    }

    @Override
    public void publish(RedisMessage message) {
        if (this.connection.isOpen()) {
            this.connection.publish(this.channel, encode(message));
            return;
        }
        throw new IllegalStateException("Redis is not connected");
    }

    @Override
    public void publishOneWay(OneWayMessage message, String targetServer) {
        if (this.connection.isOpen()) {
            message.setTargetServer(targetServer);
            this.connection.publish(this.channel, encode(message));
            return;
        }
        throw new IllegalStateException("Redis is not connected");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends TwoWayResponseMessage> CompletableFuture<R> publishTwoWay(TwoWayRequestMessage<R> message, String targetServer) {
        if (this.connection.isOpen()) {
            CompletableFuture<TwoWayResponseMessage> future = new CompletableFuture<>();
            TimeStampedFuture<TwoWayResponseMessage> timeStampedFuture = new TimeStampedFuture<>(System.currentTimeMillis(), future);
            this.connection.nextMessageId().whenComplete((nextId, t) -> {
                if (t != null) {
                    this.logger.error("Failed to get next message id", t);
                    return;
                }
                this.pendingResponses.put(nextId, timeStampedFuture);
                message.setMessageId(nextId);
                message.setSourceServer(this.serverId);
                message.setTargetServer(targetServer);
                this.connection.publish(this.channel, encode(message));
            });
            return (CompletableFuture<R>) future;
        }
        throw new IllegalStateException("Redis is not connected");
    }

    @Override
    public void response(TwoWayResponseMessage message) {
        TimeStampedFuture<TwoWayResponseMessage> remove = this.pendingResponses.getIfPresent(message.messageId());
        if (remove != null) {
            remove.future().complete(message);
            this.pendingResponses.invalidate(message.messageId());
        }
    }

    @Override
    public byte[] encode(RedisMessage message) {
        RegisteredRedisMessage<ByteBuf, RedisMessage> registered = this.registry.byId(message.identifier());
        if (registered == null) {
            throw new IllegalArgumentException("Message with id " + message.identifier() + " does not exist");
        }

        ByteBuf buf = null;
        try {
            buf = this.allocator.buffer(message.estimateSize());

            ByteBufHelper.writeCompactInt(buf, registered.id());
            registered.codec().encode(new SparrowByteBuf(buf), message);

            // 直接转换为字节数组，避免额外的内存拷贝
            if (buf.hasArray() && buf.arrayOffset() == 0 && buf.readableBytes() == buf.array().length) {
                return buf.array();
            } else {
                byte[] result = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), result);
                return result;
            }
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }

    @Override
    public RedisMessage decode(byte[] bytes) {
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        int id = ByteBufHelper.readCompactInt(buf);
        RegisteredRedisMessage<ByteBuf, RedisMessage> registered = this.registry.byId(id);
        if (registered == null) {
            return null;
        }
        return registered.codec().decode(new SparrowByteBuf(buf));
    }

    @Override
    public void unsubscribe() {
        if (this.connection.isOpen()) {
            this.connection.unsubscribe(this.channel);
        }
    }

    @Override
    public void subscribe() {
        if (this.connection.isOpen()) {
            this.connection.subscribe(this.channel, (byte[] message) -> {
                RedisMessage decode = decode(message);
                if (decode != null) {
                    decode.executor().execute(() -> decode.handle(this));
                }
            });
            return;
        }
        throw new IllegalStateException("Redis is not connected");
    }

    @Override
    public RedisConnection connection() {
        return this.connection;
    }
}
