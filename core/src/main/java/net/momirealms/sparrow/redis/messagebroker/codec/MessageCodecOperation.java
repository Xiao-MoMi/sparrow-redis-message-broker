package net.momirealms.sparrow.redis.messagebroker.codec;

@FunctionalInterface
public interface MessageCodecOperation<B, T, M> {

    MessageCodec<B, M> apply(MessageCodec<B, T> codec);
}
