package carpet.helpers.lifetime.trackeddata;

import carpet.helpers.lifetime.removal.RemovalReason;
import carpet.helpers.lifetime.spawning.SpawningReason;
import carpet.helpers.lifetime.utils.CounterUtil;
import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;

public abstract class ExtraCountTrackedData extends BasicTrackedData
{
    public final Map<SpawningReason, Long> spawningExtraCountMap = Maps.newHashMap();
    public final Map<RemovalReason, Long> removalExtraCountMap = Maps.newHashMap();

    protected abstract long getExtraCount(Entity entity);

    @Override
    public void updateSpawning(Entity entity, SpawningReason reason)
    {
        super.updateSpawning(entity, reason);
        this.spawningExtraCountMap.put(reason, this.spawningExtraCountMap.getOrDefault(reason, 0L) + this.getExtraCount(entity));
    }

    @Override
    public void updateRemoval(Entity entity, RemovalReason reason)
    {
        super.updateRemoval(entity, reason);
        this.removalExtraCountMap.put(reason, this.removalExtraCountMap.getOrDefault(reason, 0L) + this.getExtraCount(entity));
    }

    protected abstract String getCountDisplayString();

    private ITextComponent attachExtraCountHoverText(ITextComponent text, long extraCount, long ticks)
    {
        return TextUtil.attachHoverText(text, Messenger.c(
                "w " + this.getCountDisplayString(),
                "g : ",
                CounterUtil.ratePerHourText(extraCount, ticks, "wgg")
        ));
    }

    @Override
    public ITextComponent getSpawningCountText(long ticks)
    {
        return this.attachExtraCountHoverText(super.getSpawningCountText(ticks), getLongMapSum(this.spawningExtraCountMap), ticks);
    }

    @Override
    public ITextComponent getRemovalCountText(long ticks)
    {
        return this.attachExtraCountHoverText(super.getRemovalCountText(ticks), getLongMapSum(this.removalExtraCountMap), ticks);
    }

    @Override
    protected ITextComponent getSpawningReasonWithRate(SpawningReason reason, long ticks, long count, long total)
    {
        return this.attachExtraCountHoverText(super.getSpawningReasonWithRate(reason, ticks, count, total), this.spawningExtraCountMap.getOrDefault(reason, 0L), ticks);
    }

    @Override
    protected ITextComponent getRemovalReasonWithRate(RemovalReason reason, long ticks, long count, long total)
    {
        return this.attachExtraCountHoverText(super.getRemovalReasonWithRate(reason, ticks, count, total), this.removalExtraCountMap.getOrDefault(reason, 0L), ticks);
    }
}
