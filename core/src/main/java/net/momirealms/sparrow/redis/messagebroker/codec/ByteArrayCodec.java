package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.momirealms.sparrow.redis.messagebroker.util.ByteBufHelper;

public class ByteArrayCodec implements MessageCodec<ByteBuf, byte[]> {
    private final int maxSize;

    public ByteArrayCodec(int maxSize) {
        this.maxSize = maxSize;
    }

    public static ByteArrayCodec byteArray(int maxSize) {
        return new ByteArrayCodec(maxSize);
    }

    @Override
    public byte[] decode(ByteBuf buffer) {
        return ByteBufHelper.readByteArray(buffer, this.maxSize);
    }

    @Override
    public void encode(ByteBuf buffer, byte[] value) {
        int actualSize = value.length;
        if (actualSize > this.maxSize) {
            throw new EncoderException(String.format(
                    "Byte array size exceeds maximum allowed: %d > %d bytes",
                    actualSize, this.maxSize));
        }
        ByteBufHelper.writeByteArray(buffer, value);
    }
}
