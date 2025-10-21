package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class LongCodec implements MessageCodec<ByteBuf, Long> {
    public static final LongCodec INSTANCE = new LongCodec();

    private LongCodec() {}

    @Override
    public Long decode(ByteBuf buffer) {
        return buffer.readLong();
    }

    @Override
    public void encode(ByteBuf buffer, Long value) {
        buffer.writeLong(value);
    }
}
