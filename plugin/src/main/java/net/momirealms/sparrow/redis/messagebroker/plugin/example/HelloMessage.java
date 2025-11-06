package net.momirealms.sparrow.redis.messagebroker.plugin.example;

import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import net.momirealms.sparrow.redis.messagebroker.executors.MessageExecutors;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class HelloMessage implements RedisMessage {
    public static final MessageCodec<SparrowByteBuf, HelloMessage> CODEC = MessageCodec.ofMember(HelloMessage::write, HelloMessage::new);
    public static final MessageIdentifier ID = MessageIdentifier.of("sparrow", "hello");
    private final String message;

    public HelloMessage(String message) {
        this.message = message;
    }

    private HelloMessage(SparrowByteBuf buf) {
        this.message = buf.readUtf8();
    }

    private void write(SparrowByteBuf buf) {
        buf.writeUtf8(this.message);
    }

    @Override
    public @NotNull MessageIdentifier identifier() {
        return ID;
    }

    @Override
    public void handle(MessageBroker broker) {
    }

    @Override
    public Executor executor() {
        return MessageExecutors.VIRTUAL;
    }
}
