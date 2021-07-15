package carpet.helpers.lifetime.utils;

import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.DimensionType;

public class LifeTimeStatistic
{
    public static final String COLOR_MIN_TIME = "q ";
    public static final String COLOR_MAX_TIME = "c ";
    public static final String COLOR_AVG_TIME = "p ";

    public StatisticElement minTimeElement;
    public StatisticElement maxTimeElement;
    public long count;
    public long timeSum;

    public LifeTimeStatistic()
    {
        this.clear();
    }

    public void clear()
    {
        this.count = 0;
        this.timeSum = 0;
        this.minTimeElement = new StatisticElement(Integer.MAX_VALUE, null, null, null);
        this.maxTimeElement = new StatisticElement(Integer.MIN_VALUE, null, null, null);
    }

    public boolean isValid()
    {
        return this.count > 0;
    }

    public void update(Entity entity)
    {
        long time = entity.getLifeTime();
        this.count++;
        this.timeSum += time;
        StatisticElement element = new StatisticElement(time, entity.getEntityWorld().provider.getDimensionType(), entity.getSpawningPosition(), entity.getRemovalPosition());
        if (time < this.minTimeElement.time)
        {
            this.minTimeElement = element;
        }
        if (time > this.maxTimeElement.time)
        {
            this.maxTimeElement = element;
        }
    }

    /**
     * - Minimum life time: xx gt
     * - Maximum life time: yy gt
     * - Average life time: zz gt
     *
     * @param indentString spaces for indent
     */
    public ITextComponent getResult(String indentString, boolean hoverMode)
    {
        ITextComponent indent = Messenger.s(null, indentString, "g");
        ITextComponent newLine = Messenger.s(null, "\n");
        if (!this.isValid())
        {
            return Messenger.c(indent, "g   N/A");
        }
        indent = Messenger.c(indent, "g - ");
        return Messenger.c(
                indent,
                this.minTimeElement.getTimeWithPos("Minimum life time", COLOR_MIN_TIME, hoverMode),
                newLine,
                indent,
                this.maxTimeElement.getTimeWithPos("Maximum life time", COLOR_MAX_TIME, hoverMode),
                newLine,
                indent,
                "w Average life time",
                "g : ",
                COLOR_AVG_TIME + String.format("%.4f", (double)this.timeSum / this.count),
                "g  gt"
        );
    }

    public ITextComponent getCompressedResult(boolean showGtSuffix)
    {
        if (!this.isValid())
        {
            return Messenger.s(null, "N/A", "g");
        }
        ITextComponent text = Messenger.c(
                COLOR_MIN_TIME + this.minTimeElement.time,
                "g /",
                COLOR_MAX_TIME + this.maxTimeElement.time,
                "g /",
                COLOR_AVG_TIME + String.format("%.2f", (double)this.timeSum / this.count)
        );
        if (showGtSuffix)
        {
            text.appendSibling(Messenger.c("g  (gt)"));
        }
        return text;
    }

    private static class StatisticElement
    {
        private final long time;
        private final DimensionType dimensionType;
        private final Vec3d spawningPos;
        private final Vec3d removalPos;

        private StatisticElement(long time, DimensionType dimensionType, Vec3d spawningPos, Vec3d removalPos)
        {
            this.time = time;
            this.dimensionType = dimensionType;
            this.spawningPos = spawningPos;
            this.removalPos = removalPos;
        }

        /**
         * [hint]: 123 gt
         * [hint]: 123 gt [S] [R]
         */
        private ITextComponent getTimeWithPos(String hint, String fmt, boolean hoverMode)
        {
            ITextComponent text = Messenger.c(
                    "w " + hint,
                    "g : ",
                    fmt + this.time,
                    "g  gt"
            );
            if (!hoverMode)
            {
                text.appendSibling(Messenger.c(
                        "w  ",
                        TextUtil.getFancyText(
                                null,
                                Messenger.s(null, "[S]", "e"),
                                Messenger.c(
                                        "w Spawning Position",
                                        "g : ",
                                        "w " + TextUtil.getCoordinateString(this.spawningPos)
                                ),
                                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.getTeleportCommand(this.spawningPos, this.dimensionType))
                        ),
                        "w  ",
                        TextUtil.getFancyText(
                                null,
                                Messenger.s(null, "[R]", "r"),
                                Messenger.c(
                                        "w Removal Position",
                                        "g : ",
                                        "w " + TextUtil.getCoordinateString(this.removalPos)
                                ),
                                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.getTeleportCommand(this.removalPos, this.dimensionType))
                        )
                ));
            }
            return text;
        }
    }
}
