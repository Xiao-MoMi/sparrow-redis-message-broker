package net.momirealms.sparrow.redis.messagebroker.plugin.example;

import net.momirealms.sparrow.redis.messagebroker.AsyncRedisMessage;
import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;
import org.jetbrains.annotations.NotNull;

public class HelloMessage implements AsyncRedisMessage {
    public static final MessageCodec<SparrowByteBuf, HelloMessage> CODEC = MessageCodec.ofMember(HelloMessage::writeTo, HelloMessage::new);
    public static final MessageIdentifier ID = MessageIdentifier.of("sparrow", "hello");
    private final String message;

    public HelloMessage(String message) {
        this.message = message;
    }

    private HelloMessage(SparrowByteBuf buf) {
        this.message = buf.readUtf8();
    }

    private void writeTo(SparrowByteBuf buf) {
        buf.writeUtf8(this.message);
    }

    @Override
    public @NotNull MessageIdentifier id() {
        return ID;
    }

    @Override
    public void handle() {
        System.out.println("HelloMessage: " + this.message);
    }
}
