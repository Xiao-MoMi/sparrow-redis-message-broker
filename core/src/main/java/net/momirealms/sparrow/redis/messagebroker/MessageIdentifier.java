package net.momirealms.sparrow.redis.messagebroker;

public class MessageIdentifier {
    private final String namespace;
    private final String value;
    private Integer hashCode;

    public MessageIdentifier(String namespace, String value) {
        this.namespace = namespace;
        this.value = value;
    }

    public static MessageIdentifier of(final String namespace, final String id) {
        return new MessageIdentifier(namespace, id);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof MessageIdentifier that)) return false;
        return this.namespace.equals(that.namespace) && this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.namespace.hashCode() + 31 * this.value.hashCode();
        }
        return this.hashCode;
    }
}
