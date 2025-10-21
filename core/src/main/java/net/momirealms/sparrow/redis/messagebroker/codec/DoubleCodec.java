package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class DoubleCodec implements MessageCodec<ByteBuf, Double> {
    public static final DoubleCodec INSTANCE = new DoubleCodec();

    private DoubleCodec() {}

    @Override
    public Double decode(ByteBuf buffer) {
        return buffer.readDouble();
    }

    @Override
    public void encode(ByteBuf buffer, Double value) {
        buffer.writeDouble(value);
    }
}
