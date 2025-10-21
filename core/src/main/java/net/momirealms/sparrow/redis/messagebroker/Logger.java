package net.momirealms.sparrow.redis.messagebroker;

public interface Logger {

    void error(String msg, Throwable t);

    void warn(String msg, Throwable t);

    void info(String msg);

    void debug(String msg);
}
