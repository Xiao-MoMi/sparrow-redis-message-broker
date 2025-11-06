package net.momirealms.sparrow.redis.messagebroker.message;

import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;

public abstract class TwoWayResponseMessage implements RedisMessage {
    protected long messageId;
    protected String sourceServer;
    protected String targetServer;

    public TwoWayResponseMessage() {
    }

    protected TwoWayResponseMessage(SparrowByteBuf buf) {
        this.messageId = buf.readCompactLong();
        this.sourceServer = buf.readUtf8();
        this.targetServer = buf.readUtf8();
    }

    protected void write(SparrowByteBuf buf) {
        buf.writeCompactLong(messageId);
        buf.writeUtf8(sourceServer);
        buf.writeUtf8(targetServer);
    }

    public long messageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setSourceServer(String sourceServer) {
        this.sourceServer = sourceServer;
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    public String sourceServer() {
        return sourceServer;
    }

    public String targetServer() {
        return targetServer;
    }

    @Override
    public void handle(MessageBroker broker) {
        if (broker.serverId().equals(this.targetServer)) {
            broker.response(this);
        }
    }
}
