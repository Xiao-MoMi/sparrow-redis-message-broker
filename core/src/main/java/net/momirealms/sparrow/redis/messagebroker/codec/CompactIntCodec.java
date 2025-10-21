package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.util.ByteBufHelper;

public class CompactIntCodec implements MessageCodec<ByteBuf, Integer> {
    public static final CompactIntCodec INSTANCE = new CompactIntCodec();

    private CompactIntCodec() {}

    @Override
    public Integer decode(ByteBuf buffer) {
        return ByteBufHelper.readCompactInt(buffer);
    }

    @Override
    public void encode(ByteBuf buffer, Integer value) {
        ByteBufHelper.writeCompactInt(buffer, value);
    }
}
