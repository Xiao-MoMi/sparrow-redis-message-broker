package net.momirealms.sparrow.redis.messagebroker;

public class RedisMessageExecutionException extends RuntimeException {

    public RedisMessageExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
