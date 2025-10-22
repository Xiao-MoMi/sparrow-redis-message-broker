package net.momirealms.sparrow.redis.messagebroker.registry;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultMessageRegistry implements RedisMessageRegistry {
    private final List<RegisteredRedisMessage<ByteBuf, RedisMessage>> byId;
    private final Map<MessageIdentifier, RegisteredRedisMessage<ByteBuf, RedisMessage>> byKey;

    public DefaultMessageRegistry(int expectedSize) {
        this.byId = new ArrayList<>(expectedSize);
        this.byKey = new HashMap<>(expectedSize);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <B extends ByteBuf, M extends RedisMessage> void register(@NotNull MessageIdentifier id, @NotNull MessageCodec<B, M> codec) {
        if (this.byKey.containsKey(id)) {
            throw new IllegalStateException("Message already registered: " + id);
        }

        RegisteredRedisMessage<B, M> type = new RegisteredRedisMessage<>(this.byId.size(), id, codec);
        this.byKey.put(id, (RegisteredRedisMessage) type);
        this.byId.add((RegisteredRedisMessage) type);
    }

    @Override
    public RegisteredRedisMessage<ByteBuf, RedisMessage> byId(@NotNull MessageIdentifier id) {
        return this.byKey.get(id);
    }

    @Override
    public RegisteredRedisMessage<ByteBuf, RedisMessage> byId(int id) {
        if (id >= this.byId.size()) return null;
        return this.byId.get(id);
    }

    @Override
    public boolean contains(@NotNull MessageIdentifier id) {
        return this.byKey.containsKey(id);
    }
}
