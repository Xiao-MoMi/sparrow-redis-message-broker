package net.momirealms.sparrow.redis.messagebroker.registry;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RedisMessageRegistry {

    <B extends ByteBuf, M extends RedisMessage> RegisteredRedisMessage<B, M> register(@NotNull MessageIdentifier id, @NotNull MessageCodec<B, M> codec);

    @Nullable
    RegisteredRedisMessage<ByteBuf, RedisMessage> byId(@NotNull MessageIdentifier id);

    @Nullable
    RegisteredRedisMessage<ByteBuf, RedisMessage> byId(int id);

    boolean contains(@NotNull MessageIdentifier id);
}
