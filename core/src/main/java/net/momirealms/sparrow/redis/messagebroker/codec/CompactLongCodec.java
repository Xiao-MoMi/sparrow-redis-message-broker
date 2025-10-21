package net.momirealms.sparrow.redis.messagebroker.codec;

import io.netty.buffer.ByteBuf;
import net.momirealms.sparrow.redis.messagebroker.util.ByteBufHelper;

public class CompactLongCodec implements MessageCodec<ByteBuf, Long> {
    public static final CompactLongCodec INSTANCE = new CompactLongCodec();

    private CompactLongCodec() {}

    @Override
    public Long decode(ByteBuf buffer) {
        return ByteBufHelper.readCompactLong(buffer);
    }

    @Override
    public void encode(ByteBuf buffer, Long value) {
        ByteBufHelper.writeCompactLong(buffer, value);
    }
}
