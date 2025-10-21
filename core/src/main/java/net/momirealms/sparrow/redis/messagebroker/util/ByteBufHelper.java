package net.momirealms.sparrow.redis.messagebroker.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.nio.charset.StandardCharsets;

public final class ByteBufHelper {
    private ByteBufHelper() {}

    public static void writeUtf8(final ByteBuf buffer, final String string, int maxLength) {
        // 检查字符串长度是否超过最大允许值
        int stringLength = string.length();
        if (stringLength > maxLength) {
            throw new EncoderException(String.format(
                    "String length exceeds maximum allowed: %d > %d characters",
                    stringLength, maxLength));
        }

        // 计算字符串编码所需的最大字节数
        int maxBytesForString = ByteBufUtil.utf8MaxBytes(string);
        ByteBuf tempBuffer = buffer.alloc().buffer(maxBytesForString);

        try {
            // 将字符串写入临时缓冲区
            int actualEncodedBytes = ByteBufUtil.writeUtf8(tempBuffer, string);

            // 检查编码后的字节数是否超过最大限制
            int maxAllowedBytes = ByteBufUtil.utf8MaxBytes(maxLength);
            if (actualEncodedBytes > maxAllowedBytes) {
                throw new EncoderException(String.format(
                        "Encoded string size exceeds maximum allowed: %d > %d bytes (original length: %d characters)",
                        actualEncodedBytes, maxAllowedBytes, stringLength));
            }

            // 写入长度和内容
            ByteBufHelper.writeCompactInt(buffer, actualEncodedBytes);
            buffer.writeBytes(tempBuffer);
        } finally {
            // 确保临时缓冲区被释放
            tempBuffer.release();
        }
    }

    public static String readUtf8(final ByteBuf buffer, int maxLength) {
        int maxUtf8Bytes = ByteBufUtil.utf8MaxBytes(maxLength);
        int encodedLength = ByteBufHelper.readCompactInt(buffer);

        // 检查编码长度是否超过最大允许值
        if (encodedLength > maxUtf8Bytes) {
            throw new DecoderException(String.format(
                    "Encoded string length exceeds maximum allowed: %d > %d (max characters: %d)",
                    encodedLength, maxUtf8Bytes, maxLength));
        }

        // 检查编码长度是否为负数
        if (encodedLength < 0) {
            throw new DecoderException("Invalid encoded string length: " + encodedLength + " (negative length)");
        }

        // 检查缓冲区是否有足够字节
        int availableBytes = buffer.readableBytes();
        if (encodedLength > availableBytes) {
            throw new DecoderException(String.format(
                    "Insufficient bytes in buffer: required %d, available %d",
                    encodedLength, availableBytes));
        }

        // 解码字符串
        String decodedString = buffer.toString(buffer.readerIndex(), encodedLength, StandardCharsets.UTF_8);
        buffer.readerIndex(buffer.readerIndex() + encodedLength);

        // 检查解码后的字符串长度
        if (decodedString.length() > maxLength) {
            throw new DecoderException(String.format(
                    "Decoded string length exceeds maximum allowed: %d > %d characters",
                    decodedString.length(), maxLength));
        }

        return decodedString;
    }

    public static byte[] readByteArray(ByteBuf buffer, int maxSize) {
        int i = readCompactInt(buffer);
        if (i > maxSize) {
            throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxSize);
        }
        byte[] bytes = new byte[i];
        buffer.readBytes(bytes);
        return bytes;
    }

    public static void writeByteArray(ByteBuf buffer, byte[] bytes) {
        writeCompactInt(buffer, bytes.length);
        buffer.writeBytes(bytes);
    }

    public static int readCompactInt(ByteBuf buffer) {
        int value = 0;
        int shift = 0;
        byte byteValue;
        do {
            byteValue = buffer.readByte();
            value |= (byteValue & 127) << shift++ * 7;
            if (shift > 5) {
                throw new DecoderException("CompactInt is bigger than expected(5)");
            }
        } while ((byteValue & 128) == 128);
        return value;
    }

    public static void writeCompactInt(ByteBuf buffer, int value) {
        while ((value & -128) != 0) {
            buffer.writeByte((value & 127) | 128);
            value >>>= 7;
        }
        buffer.writeByte(value & 127);
    }

    public static long readCompactLong(ByteBuf buffer) {
        long value = 0L;
        int shift = 0;
        byte byteValue;
        do {
            byteValue = buffer.readByte();
            value |= (long) (byteValue & 127) << shift++ * 7;
            if (shift > 10) {
                throw new DecoderException("CompactLong is bigger than expected(10)");
            }
        } while ((byteValue & 128) == 128);
        return value;
    }

    public static void writeCompactLong(ByteBuf buffer, long value) {
        while ((value & -128L) != 0L) {
            buffer.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }
        buffer.writeByte((int) (value & 127L));
    }
}
