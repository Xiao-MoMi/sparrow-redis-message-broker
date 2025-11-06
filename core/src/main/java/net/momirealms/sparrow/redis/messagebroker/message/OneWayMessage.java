package net.momirealms.sparrow.redis.messagebroker.message;

import net.momirealms.sparrow.redis.messagebroker.MessageBroker;
import net.momirealms.sparrow.redis.messagebroker.RedisMessage;
import net.momirealms.sparrow.redis.messagebroker.util.SparrowByteBuf;

/**
 * 单向消息，适用于一对一，一对多等情形
 * 如果目标服务器id为空，则为广播模式
 * 如果指定了目标服务器id，则只发给目标服务器
 * 如果id以#开头，则代表匹配服务器标签
 */
public abstract class OneWayMessage implements RedisMessage {
    protected final String targetServer;

    protected OneWayMessage(String targetServer) {
        this.targetServer = targetServer;
    }

    protected OneWayMessage(SparrowByteBuf buf) {
        this.targetServer = buf.readUtf8();
    }

    @Override
    public void handle(MessageBroker broker) {
        if (this.targetServer.isEmpty()) {
            handle();
        } else {
            if (this.targetServer.charAt(0) == '#') {
                if (broker.tags().contains(this.targetServer.substring(1))) {
                    handle();
                }
            } else {
                if (this.targetServer.equals(broker.serverId())) {
                    handle();
                }
            }
        }
    }

    protected abstract void handle();
}
