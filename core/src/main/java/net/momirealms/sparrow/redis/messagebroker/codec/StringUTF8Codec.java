package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.util.ByteBufHelper;

public class StringUTF8Codec implements MessageCodec<ByteBuf, String> {
    private final int maxLength;

    private StringUTF8Codec(int maxLength) {
        this.maxLength = maxLength;
    }

    public static StringUTF8Codec string(int maxLength) {
        return new StringUTF8Codec(maxLength);
    }

    @Override
    public String decode(ByteBuf buffer) {
        return ByteBufHelper.readUtf8(buffer, this.maxLength);
    }

    @Override
    public void encode(ByteBuf buffer, String string) {
        ByteBufHelper.writeUtf8(buffer, string, this.maxLength);
    }
}
