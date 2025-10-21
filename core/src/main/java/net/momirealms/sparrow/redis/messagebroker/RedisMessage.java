package net.momirealms.sparrow.redis.messagebroker;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageDecoder;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageMemberEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public interface RedisMessage {

    @NotNull
    MessageIdentifier id();

    void handle();

    default Executor executor() {
        return Runnable::run;
    }

    static <B extends ByteBuf, T extends RedisMessage> MessageCodec<B, T> codec(MessageMemberEncoder<B, T> encoder, MessageDecoder<B, T> decoder) {
        return MessageCodec.ofMember(encoder, decoder);
    }
}
