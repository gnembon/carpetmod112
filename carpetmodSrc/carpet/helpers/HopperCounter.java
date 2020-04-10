package carpet.helpers;

import carpet.CarpetServer;
import carpet.pubsub.PubSubInfoProvider;
import carpet.utils.Messenger;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HopperCounter
{
    public static final HopperCounter cactus = new HopperCounter(EnumDyeColor.GREEN, "cactus");
    public static final Map<String, HopperCounter> COUNTERS;

    static {
        COUNTERS = new HashMap<>();
        for (EnumDyeColor color : EnumDyeColor.values()) {
            COUNTERS.put(color.getName(), new HopperCounter(color, color.getName()));
        }
        COUNTERS.put("cactus", cactus);
    }

    public final EnumDyeColor color;
    private final Object2LongMap<ItemWithMeta> counter = new Object2LongLinkedOpenHashMap<>();
    private long startTick;
    private long startMillis;
    private PubSubInfoProvider<Long> pubSubProvider;
    private String name;

    private HopperCounter(EnumDyeColor color, String name) {
        this.name = name;
        this.color = color;
        pubSubProvider = new PubSubInfoProvider<>(CarpetServer.PUBSUB, "carpet.counter." + name, 0, this::getTotalItems);
    }

    public void add(MinecraftServer server, ItemStack stack) {
        if (startTick == 0) {
            startTick = server.getTickCounter();
            startMillis = MinecraftServer.getCurrentTimeMillis();
        }
        ItemWithMeta item = new ItemWithMeta(stack);
        counter.put(item, counter.getLong(item) + stack.getCount());
        pubSubProvider.publish();
    }

    public void reset(MinecraftServer server) {
        counter.clear();
        startTick = server.getTickCounter();
        startMillis = MinecraftServer.getCurrentTimeMillis();
        pubSubProvider.publish();
    }

    public static void resetAll(MinecraftServer server) {
        for (HopperCounter counter : COUNTERS.values()) {
            counter.reset(server);
        }
    }

    public static List<ITextComponent> formatAll(MinecraftServer server, boolean realtime)
    {
        List<ITextComponent> text = new ArrayList<>();

        for (HopperCounter counter : COUNTERS.values()) {
            List<ITextComponent> temp = counter.format(server, realtime, false);
            if (temp.size() > 1) {
                text.addAll(temp);
            }
        }
        if (text.isEmpty()) {
            text.add(Messenger.s(null, "No items have been counted yet."));
        }
        return text;
    }

    public List<ITextComponent> format(MinecraftServer server, boolean realTime, boolean brief) {
        if (counter.isEmpty()) {
            if (brief) {
                return Collections.singletonList(Messenger.m(null, "g "+name+": -, -/h, - min "));
            }
            return Collections.singletonList(Messenger.s(null, String.format("No items for %s yet", name)));
        }
        long total = getTotalItems();
        long ticks = Math.max(realTime ? (MinecraftServer.getCurrentTimeMillis() - startMillis) / 50 : server.getTickCounter() - startTick, 1);
        if (total == 0) {
            if (brief) {
                return Collections.singletonList(Messenger.m(null,
                        String.format("c %s: 0, 0/h, %.1f min ", name, ticks / (20.0 * 60.0))));
            }
            return Collections.singletonList(Messenger.m(null,
                    String.format("w No items for %s yet (%.2f min.%s)",
                            name, ticks / (20.0 * 60.0), (realTime ? " - real time" : "")),
                    "nb  [X]", "^g reset", "!/counter " + name +" reset"));
        }
        if (brief) {
            return Collections.singletonList(Messenger.m(null,
                    String.format("c %s: %d, %d/h, %.1f min ",
                            name, total, total * (20 * 60 * 60) / ticks, ticks / (20.0 * 60.0))));
        }
        List<ITextComponent> list = counter.entrySet().stream().map(e -> {
            String itemName = e.getKey().getDisplayName();
            long count = e.getValue();
            return Messenger.s(null, String.format(" - %s: %d, %.1f/h",
                    itemName,
                    count,
                    count * (20.0 * 60.0 * 60.0) / ticks));
        }).collect(Collectors.toList());
        list.add(0, Messenger.s(null, String.format("Counter: %s", name)));
        return list;
    }

    @Nullable
    public static HopperCounter getCounter(String color) {
        try {
            return COUNTERS.get(color);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getTotalItems() {
        return counter.values().stream().mapToLong(Long::longValue).sum();
    }
}
