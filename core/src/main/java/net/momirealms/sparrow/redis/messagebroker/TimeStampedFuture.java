package net.momirealms.sparrow.redis.messagebroker;

import java.util.concurrent.CompletableFuture;

public record TimeStampedFuture<T>(long time, CompletableFuture<T> future) {

    boolean isExpired(long time) {
        return System.currentTimeMillis() - time > time;
    }
}
