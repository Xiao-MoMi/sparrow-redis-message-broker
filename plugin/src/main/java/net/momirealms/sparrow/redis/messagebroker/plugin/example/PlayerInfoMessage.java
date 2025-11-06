package net.momirealms.sparrow.redis.messagebroker.plugin.example;

import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import net.momirealms.sparrow.redis.messagebroker.executors.MessageExecutors;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.Executor;

public class PlayerInfoMessage implements RedisMessage {
    public static final MessageCodec<SparrowByteBuf, PlayerInfoMessage> CODEC = MessageCodec.ofMember(PlayerInfoMessage::write, PlayerInfoMessage::new);
    public static final MessageIdentifier ID = MessageIdentifier.of("sparrow", "player_info");
    private final String name;
    private final UUID uuid;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;

    public PlayerInfoMessage(String name, UUID uuid, String world, double x, double y, double z, float pitch, float yaw) {
        this.name = name;
        this.uuid = uuid;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    private PlayerInfoMessage(SparrowByteBuf buf) {
        this.name = buf.readUtf8();
        this.uuid = buf.readUUID();
        this.world = buf.readUtf8();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.pitch = buf.readFloat();
        this.yaw = buf.readFloat();
    }

    private void write(SparrowByteBuf buf) {
        buf.writeUtf8(this.name);
        buf.writeUUID(this.uuid);
        buf.writeUtf8(this.world);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.pitch);
        buf.writeFloat(this.yaw);
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
