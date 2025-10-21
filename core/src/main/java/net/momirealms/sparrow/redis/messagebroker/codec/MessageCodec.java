package net.momirealms.sparrow.redis.messagebroker.codec;

public interface MessageCodec<B, M> extends MessageDecoder<B, M>, MessageEncoder<B, M> {

    static <B, M> MessageCodec<B, M> of(MessageEncoder<B, M> encoder, MessageDecoder<B, M> decoder) {
        return new SimpleMessageCodec<>(encoder, decoder);
    }

    static <B, M> MessageCodec<B, M> ofMember(MessageMemberEncoder<B, M> encoder, MessageDecoder<B, M> decoder) {
        return new SimpleMemberMessageCodec<>(encoder, decoder);
    }

    class SimpleMessageCodec<B, M> implements MessageCodec<B, M> {
        private final MessageEncoder<B, M> encoder;
        private final MessageDecoder<B, M> decoder;

        public SimpleMessageCodec(MessageEncoder<B, M> encoder, MessageDecoder<B, M> decoder) {
            this.decoder = decoder;
            this.encoder = encoder;
        }

        @Override
        public M decode(B buffer) {
            return this.decoder.decode(buffer);
        }

        @Override
        public void encode(B buffer, M message) {
            this.encoder.encode(buffer, message);
        }
    }

    class SimpleMemberMessageCodec<B, M> implements MessageCodec<B, M> {
        private final MessageMemberEncoder<B, M> encoder;
        private final MessageDecoder<B, M> decoder;

        public SimpleMemberMessageCodec(MessageMemberEncoder<B, M> encoder, MessageDecoder<B, M> decoder) {
            this.encoder = encoder;
            this.decoder = decoder;
        }

        @Override
        public M decode(B buffer) {
            return this.decoder.decode(buffer);
        }

        @Override
        public void encode(B buffer, M message) {
            this.encoder.encode(message, buffer);
        }
    }
}
