package carpet.helpers;

import carpet.CarpetServer;
import carpet.pubsub.PubSubInfoProvider;
import carpet.utils.Messenger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GolemCounter {
    public static final GolemCounter counter = new GolemCounter();
    private long golems;
    private final Map<Integer, Integer> cap = new TreeMap<>();
    private long fails;
    private long total;
    private long startTick;
    private long startMillis;
    private final PubSubInfoProvider<Long> pubSubProvider;

    private GolemCounter() {
        pubSubProvider = new PubSubInfoProvider<>(CarpetServer.PUBSUB, "carpet.golems", 0, this::getGolems);
    }

    public void add(MinecraftServer server, int missed) {
        if (startTick == 0) {
            startTick = server.getTickCounter();
            startMillis = MinecraftServer.getCurrentTimeMillis();
        }
        this.cap.merge(missed, 1, Integer::sum);
        this.pubSubProvider.publish();
    }

    public void incrementGolems() {
        this.golems++;
    }

    public void incrementFails() {
        this.fails++;
    }

    public void incrementTotal() {
        this.total++;
    }

    public void reset(MinecraftServer server) {
        this.golems = 0;
        this.cap.clear();
        this.fails = 0;
        this.total = 0;
        this.startTick = server.getTickCounter();
        this.startMillis = MinecraftServer.getCurrentTimeMillis();
        this.pubSubProvider.publish();
    }

    public ITextComponent[] format(MinecraftServer server, boolean realtime, boolean brief, boolean compact) {
        long ticks = Math.max(realtime ? (MinecraftServer.getCurrentTimeMillis() - startMillis) / 50 : server.getTickCounter() - startTick, 1);
        if (this.golems == 0) return new ITextComponent[]{Messenger.m(null,
                String.format("w No golems counted yet, %.1f min",
                        ticks / (20.0 * 60.0)))};
        List<ITextComponent> list = new ArrayList<>();
        list.add(Messenger.m(null,
                String.format("w Golems: %d/%.2f, %.1f/h, %.1f min",
                        golems, golems + getCap() / 7000, (double) golems / ticks * (20 * 60 * 60), ticks / (20.0 * 60.0))));
        if (brief) return list.toArray(new ITextComponent[0]);

        list.add(Messenger.m(null,
                String.format("w Failed: %d/%d (%.5f%%%%)",
                        fails, fails + golems, (double) fails / (fails + golems) * 100)));
        list.add(Messenger.m(null,
                String.format("w Total: %d/%d (1/%.2f)",
                        fails + golems, total, (double) total / (fails + golems))));
        if (realtime) return list.toArray(new ITextComponent[0]);

        if (compact && cap.size() > 16) {
            int start = -1;
            int last = -1;
            int sum = 0;
            long threshold = ticks / 5000;
            for (Map.Entry<Integer, Integer> entry : cap.entrySet()) {
                int villages = entry.getKey();
                int count = entry.getValue();
                if (count < threshold) {
                    if (start == -1) start = villages;
                    last = villages;
                    sum += count;
                } else {
                    if (start != -1) {
                        if (start == last) {
                            list.add(Messenger.m(null, String.format("w  - %d: %d (%.2f%%%%)",
                                    start, sum, (double) sum / ticks * 100)));
                        } else {
                            list.add(Messenger.m(null, String.format("w  - %d-%d: %d (%.2f%%%%)",
                                    start, last, sum, (double) sum / ticks * 100)));
                        }
                    }
                    start = -1;
                    last = -1;
                    sum = 0;
                    list.add(Messenger.m(null, String.format("w  - %d: %d (%.2f%%%%)",
                            villages, count, (double) count / ticks * 100)));
                }
            }
        } else {
            cap.forEach((villages, count) ->
                    list.add(Messenger.m(null, String.format("w  - %d: %d (%.2f%%%%)",
                            villages, count, (double) count / ticks * 100))));
        }
        return list.toArray(new ITextComponent[0]);
    }

    public long getGolems() {
        return this.golems;
    }

    public double getCap() {
        double sum = 0;
        for (Map.Entry<Integer, Integer> entry : cap.entrySet()) {
            sum += (long) entry.getKey() * entry.getValue();
        }
        return sum;
    }
}