package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class BooleanCodec implements MessageCodec<ByteBuf, Boolean> {
    public static final BooleanCodec INSTANCE = new BooleanCodec();

    private BooleanCodec() {}

    @Override
    public Boolean decode(ByteBuf buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, Boolean value) {
        buffer.writeBoolean(value);
    }
}
