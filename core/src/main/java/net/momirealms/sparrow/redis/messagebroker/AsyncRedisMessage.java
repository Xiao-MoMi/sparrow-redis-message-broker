package net.momirealms.sparrow.redis.messagebroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public interface AsyncRedisMessage extends RedisMessage {
    Logger LOGGER = LoggerFactory.getLogger(AsyncRedisMessage.class);

    @Override
    default Executor executor() {
        return (runnable) -> Thread.ofVirtual().start(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                LOGGER.error("Failed to handle redis message {}", id(), e);
            }
        });
    }
}
