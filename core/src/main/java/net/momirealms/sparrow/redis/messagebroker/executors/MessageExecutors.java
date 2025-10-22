package net.momirealms.sparrow.redis.messagebroker.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public final class MessageExecutors {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageExecutors.class);

    public static final Executor SYNC = Runnable::run;
    public static final Executor VIRTUAL = (runnable) -> Thread.ofVirtual().start(() -> {
        try {
            runnable.run();
        } catch (Exception e) {
            LOGGER.error("Failed to handle redis message", e);
        }
    });
}
