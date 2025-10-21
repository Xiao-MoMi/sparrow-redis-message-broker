package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;

public class ByteCodec implements MessageCodec<ByteBuf, Byte> {
    public static final ByteCodec INSTANCE = new ByteCodec();

    private ByteCodec() {}

    @Override
    public Byte decode(ByteBuf buffer) {
        return buffer.readByte();
    }

    @Override
    public void encode(ByteBuf buffer, Byte value) {
        buffer.writeByte(value);
    }
}
