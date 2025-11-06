package net.momirealms.sparrow.redis.messagebroker.plugin.example;

import net.momirealms.sparrow.redis.messagebroker.MessageIdentifier;
import net.momirealms.sparrow.redis.messagebroker.codec.MessageCodec;
import net.momirealms.sparrow.redis.messagebroker.message.TwoWayResponseMessage;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;
import org.jetbrains.annotations.NotNull;

public class PlayerCountResponseMessage extends TwoWayResponseMessage {
    public static final MessageCodec<SparrowByteBuf, PlayerCountResponseMessage> CODEC = MessageCodec.ofMember(PlayerCountResponseMessage::write, PlayerCountResponseMessage::new);
    public static final MessageIdentifier ID = MessageIdentifier.of("sparrow", "player_count_response");
    private final int playerCount;

    public PlayerCountResponseMessage(int playerCount) {
        this.playerCount = playerCount;
    }

    private PlayerCountResponseMessage(final SparrowByteBuf buf) {
        super(buf);
        this.playerCount = buf.readCompactInt();
    }

    @Override
    protected void write(SparrowByteBuf buf) {
        super.write(buf);
        buf.writeCompactInt(this.playerCount);
    }

    public int playerCount() {
        return this.playerCount;
    }

    @Override
    public @NotNull MessageIdentifier identifier() {
        return ID;
    }
}
