package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class FloatCodec implements MessageCodec<ByteBuf, Float> {
    public static final FloatCodec INSTANCE = new FloatCodec();

    private FloatCodec() {}

    @Override
    public Float decode(ByteBuf buffer) {
        return buffer.readFloat();
    }

    @Override
    public void encode(ByteBuf buffer, Float value) {
        buffer.writeFloat(value);
    }
}
