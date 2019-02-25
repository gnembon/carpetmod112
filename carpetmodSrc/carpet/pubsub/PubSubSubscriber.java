package carpet.pubsub;

@FunctionalInterface
public interface PubSubSubscriber {
    void updateValue(PubSubNode node, Object value);
}
