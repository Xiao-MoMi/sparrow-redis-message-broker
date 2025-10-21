package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class IntCodec implements MessageCodec<ByteBuf, Integer> {
    public static final IntCodec INSTANCE = new IntCodec();

    private IntCodec() {}

    @Override
    public Integer decode(ByteBuf buffer) {
        return buffer.readInt();
    }

    @Override
    public void encode(ByteBuf buffer, Integer value) {
        buffer.writeInt(value);
    }
}
