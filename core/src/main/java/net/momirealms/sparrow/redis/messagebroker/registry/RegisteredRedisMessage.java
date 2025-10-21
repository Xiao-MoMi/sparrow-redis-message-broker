package net.momirealms.sparrow.redis.messagebroker.registry;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;

public record RegisteredRedisMessage<B extends ByteBuf, M extends RedisMessage>(int id, MessageIdentifier key, MessageCodec<B, M> codec) {
}
