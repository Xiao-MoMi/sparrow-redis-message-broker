package net.momirealms.sparrow.redis.messagebroker.plugin.example;

import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import net.momirealms.sparrow.redis.messagebroker.message.TwoWayRequestMessage;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PlayerCountRequestMessage extends TwoWayRequestMessage<PlayerCountResponseMessage> {
    public static final MessageCodec<SparrowByteBuf, PlayerCountRequestMessage> CODEC = MessageCodec.ofMember(PlayerCountRequestMessage::write, PlayerCountRequestMessage::new);
    public static final MessageIdentifier ID = MessageIdentifier.of("sparrow", "player_count_request");

    public PlayerCountRequestMessage() {
    }

    private PlayerCountRequestMessage(SparrowByteBuf buf) {
        super(buf);
    }

    @Override
    protected @NotNull CompletableFuture<PlayerCountResponseMessage> handleRequest() {
        return CompletableFuture.completedFuture(new PlayerCountResponseMessage(Bukkit.getOnlinePlayers().size()));
    }

    @Override
    public @NotNull MessageIdentifier identifier() {
        return ID;
    }
}
