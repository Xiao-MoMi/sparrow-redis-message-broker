package net.momirealms.sparrow.redis.messagebroker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import net.momirealms.sparrow.redis.messagebroker.connection.RedisConnection;
import net.momirealms.sparrow.redis.messagebroker.registry.RedisMessageRegistry;
import net.momirealms.sparrow.redis.messagebroker.registry.RegisteredRedisMessage;
import net.momirealms.sparrow.redis.messagebroker.util.ByteBufHelper;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;

final class MessageBrokerImpl implements MessageBroker {
    private final ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
    private final byte[] channel;
    private final RedisMessageRegistry registry;
    private final RedisConnection connection;

    public MessageBrokerImpl(byte[] channel, RedisMessageRegistry registry, RedisConnection connection) {
        this.channel = channel;
        this.registry = registry;
        this.connection = connection;
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
    public byte[] encode(RedisMessage message) {
        RegisteredRedisMessage<ByteBuf, RedisMessage> registered = this.registry.byId(message.id());
        if (registered == null) {
            throw new IllegalArgumentException("Message with id " + message.id() + " does not exist");
        }
        ByteBuf buf = null;
        try {
            buf = this.allocator.buffer(32);
            ByteBufHelper.writeCompactInt(buf, registered.id());
            registered.codec().encode(new SparrowByteBuf(buf), message);

            byte[] result = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), result);
            return result;
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
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
                ByteBuf buf = Unpooled.wrappedBuffer(message);
                int id = ByteBufHelper.readCompactInt(buf);
                RegisteredRedisMessage<ByteBuf, RedisMessage> registered = this.registry.byId(id);
                if (registered == null) {
                    return;
                }
                RedisMessage redisMessage = registered.codec().decode(new SparrowByteBuf(buf));
                redisMessage.executor().execute(redisMessage::handle);
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
