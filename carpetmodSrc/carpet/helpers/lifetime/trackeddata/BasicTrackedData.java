package carpet.helpers.lifetime.trackeddata;

import carpet.helpers.lifetime.removal.RemovalReason;
import carpet.helpers.lifetime.spawning.SpawningReason;
import carpet.helpers.lifetime.utils.AbstractReason;
import carpet.helpers.lifetime.utils.CounterUtil;
import carpet.helpers.lifetime.utils.LifeTimeStatistic;
import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A lifetime tracking tracked data per mob type
 */
public class BasicTrackedData
{
    public final Map<SpawningReason, Long> spawningReasons = Maps.newHashMap();
    public final Map<RemovalReason, LifeTimeStatistic> removalReasons = Maps.newHashMap();
    public final LifeTimeStatistic lifeTimeStatistic = new LifeTimeStatistic();

    public void updateSpawning(Entity entity, SpawningReason reason)
    {
        this.spawningReasons.put(reason, this.spawningReasons.getOrDefault(reason, 0L) + 1);
    }

    public void updateRemoval(Entity entity, RemovalReason reason)
    {
        this.lifeTimeStatistic.update(entity);
        this.removalReasons.computeIfAbsent(reason, r -> new LifeTimeStatistic()).update(entity);
    }

    protected static long getLongMapSum(Map<?, Long> longMap)
    {
        return longMap.values().stream().mapToLong(v -> v).sum();
    }

    public long getSpawningCount()
    {
        return getLongMapSum(this.spawningReasons);
    }

    public long getRemovalCount()
    {
        return this.removalReasons.values().stream().mapToLong(stat -> stat.count).sum();
    }

    /**
     * Spawn Count: xxxxx
     */
    public ITextComponent getSpawningCountText(long ticks)
    {
        return Messenger.c(
                "q  Spawn Count",
                "g : ",
                CounterUtil.ratePerHourText(this.getSpawningCount(), ticks, "wgg")
        );
    }

    /**
     * Removal Count: xxxxx
     */
    public ITextComponent getRemovalCountText(long ticks)
    {
        return Messenger.c(
                "q Removal Count",
                "g : ",
                CounterUtil.ratePerHourText(this.getRemovalCount(), ticks, "wgg")
        );
    }

    /**
     * - AAA: 50, (100/h) 25%
     * @param reason spawning reason or removal reason
     */
    private static ITextComponent getReasonWithRate(AbstractReason reason, long ticks, long count, long total)
    {
        double percentage = 100.0D * count / total;
        return Messenger.c(
                "g - ",
                reason.toText(),
                "g : ",
                CounterUtil.ratePerHourText(count, ticks, "wgg"),
                "w  ",
                TextUtil.attachHoverText(Messenger.s(null, String.format("%.1f%%", percentage)), Messenger.s(null, String.format("%.6f%%", percentage)))
        );
    }

    protected ITextComponent getSpawningReasonWithRate(SpawningReason reason, long ticks, long count, long total)
    {
        return getReasonWithRate(reason, ticks, count, total);
    }

    protected ITextComponent getRemovalReasonWithRate(RemovalReason reason, long ticks, long count, long total)
    {
        return getReasonWithRate(reason, ticks, count, total);
    }

    /**
     * Reasons for spawning
     * - AAA: 50, (100/h) 25%
     * - BBB: 150, (300/h) 75%
     *
     * @param hoverMode automatically insert a new line text between lines or not for hover text display
     * @return might be a empty list
     */
    public List<ITextComponent> getSpawningReasonsTexts(long ticks, boolean hoverMode)
    {
        List<ITextComponent> result = Lists.newArrayList();
        List<Map.Entry<SpawningReason, Long>> entryList = Lists.newArrayList(this.spawningReasons.entrySet());
        entryList.sort(Collections.reverseOrder(Comparator.comparingLong(Map.Entry::getValue)));

        // Title for hover mode
        if (!entryList.isEmpty() && hoverMode)
        {
            result.add(Messenger.s(null, "Reasons for spawning", "e"));
        }

        entryList.forEach(entry -> {
            SpawningReason reason = entry.getKey();
            Long statistic = entry.getValue();

            // added to upper result which will be sent by Messenger.send
            // so each element will be in a separate line
            if (hoverMode)
            {
                result.add(Messenger.s(null, "\n"));
            }

            result.add(this.getSpawningReasonWithRate(reason, ticks, statistic, this.getSpawningCount()));
        });
        return result;
    }

    /**
     * Reasons for removal
     * - AAA: 50, (100/h) 25%
     *   - Minimum life time: xx1 gt
     *   - Maximum life time: yy1 gt
     *   - Average life time: zz1 gt
     * - BBB: 150, (300/h) 75%
     *   - Minimum life time: xx2 gt
     *   - Maximum life time: yy2 gt
     *   - Average life time: zz2 gt
     *
     * @param hoverMode automatically insert a new line text between lines or not for hover text display
     * @return might be a empty list
     */
    public List<ITextComponent> getRemovalReasonsTexts(long ticks, boolean hoverMode)
    {
        List<ITextComponent> result = Lists.newArrayList();
        List<Map.Entry<RemovalReason, LifeTimeStatistic>> entryList = Lists.newArrayList(this.removalReasons.entrySet());
        entryList.sort(Collections.reverseOrder(Comparator.comparingLong(a -> a.getValue().count)));

        // Title for hover mode
        if (!entryList.isEmpty() && hoverMode)
        {
            result.add(Messenger.s(null, "Reasons for removal", "r"));
        }

        entryList.forEach(entry -> {
            RemovalReason reason = entry.getKey();
            LifeTimeStatistic statistic = entry.getValue();

            // added to upper result which will be sent by Messenger.send
            // so each element will be in a separate line
            if (hoverMode)
            {
                result.add(Messenger.s(null, "\n"));
            }

            result.add(Messenger.c(
                    this.getRemovalReasonWithRate(reason, ticks, statistic.count, this.lifeTimeStatistic.count),
                    "w \n",
                    statistic.getResult("  ", hoverMode)
            ));
        });
        return result;
    }
}
