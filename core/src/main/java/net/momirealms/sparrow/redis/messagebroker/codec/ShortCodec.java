package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class ShortCodec implements MessageCodec<ByteBuf, Short> {
    public static final ShortCodec INSTANCE = new ShortCodec();

    private ShortCodec() {}

    @Override
    public Short decode(ByteBuf buffer) {
        return buffer.readShort();
    }

    @Override
    public void encode(ByteBuf buffer, Short value) {
        buffer.writeShort(value);
    }
}
