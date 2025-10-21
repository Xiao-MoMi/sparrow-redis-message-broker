package net.momirealms.sparrow.redis.messagebroker.codec;

@FunctionalInterface
public interface MessageMemberEncoder<B, M> {

    void encode(M value, B buffer);
}
