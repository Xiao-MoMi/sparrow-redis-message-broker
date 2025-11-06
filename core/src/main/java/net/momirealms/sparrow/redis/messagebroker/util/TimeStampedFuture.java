package net.momirealms.sparrow.redis.messagebroker.util;

import java.util.concurrent.CompletableFuture;

public record TimeStampedFuture<T>(long time, CompletableFuture<T> future) {
}
