package net.momirealms.sparrow.redis.messagebroker.codec;

@FunctionalInterface
public interface MessageDecoder<B, M> {

    M decode(B buffer);
}
