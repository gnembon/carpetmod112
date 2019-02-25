package carpet.pubsub;

import java.util.function.Function;
import java.util.function.Supplier;

public class PubSubInfoProvider<T> implements Supplier<T> {
    public final PubSubNode node;
    public final int interval;
    private final Supplier<T> supplier;
    private final Function<PubSubInfoProvider, T> function;

    public PubSubInfoProvider(PubSubManager manager, String node, int interval, Supplier<T> supplier) {
        this(manager.getOrCreateNode(node), interval, supplier);
    }

    public PubSubInfoProvider(PubSubManager manager, String node, int interval, Function<PubSubInfoProvider, T> function) {
        this(manager.getOrCreateNode(node), interval, function);
    }

    public PubSubInfoProvider(PubSubNode node, int interval, Function<PubSubInfoProvider, T> function) {
        this.node = node;
        this.interval = interval;
        this.supplier = null;
        this.function = function;
        node.provider = this;
    }

    public PubSubInfoProvider(PubSubNode node, int interval, Supplier<T> supplier) {
        this.node = node;
        this.interval = interval;
        this.supplier = supplier;
        this.function = null;
        node.provider = this;
    }

    public boolean shouldUpdate(int tickCounter) {
        if (interval == 0) return false;
        return tickCounter % interval == 0;
    }

    public void publish() {
        this.node.publish(this.supplier.get());
    }

    public T get() {
        if (this.supplier != null) return this.supplier.get();
        return this.function.apply(this);
    }
}
