package net.momirealms.sparrow.redis.messagebroker.codec;

@FunctionalInterface
public interface MessageEncoder<B, M> {

    void encode(B buffer, M message);
}
