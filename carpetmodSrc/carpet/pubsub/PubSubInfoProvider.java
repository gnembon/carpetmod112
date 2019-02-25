package carpet.pubsub;

import java.util.function.Supplier;

public class PubSubInfoProvider<T> {
    public final PubSubNode node;
    public final int interval;
    public final Supplier<T> supplier;

    public PubSubInfoProvider(PubSubManager manager, String node, int interval, Supplier<T> supplier) {
        this(manager.getOrCreateNode(node), interval, supplier);
    }

    public PubSubInfoProvider(PubSubNode node, int interval, Supplier<T> supplier) {
        this.node = node;
        this.interval = interval;
        this.supplier = supplier;
        node.provider = this;
    }

    public boolean shouldUpdate(int tickCounter) {
        if (interval == 0) return false;
        return tickCounter % interval == 0;
    }

    public void publish() {
        this.node.publish(this.supplier.get());
    }
}
