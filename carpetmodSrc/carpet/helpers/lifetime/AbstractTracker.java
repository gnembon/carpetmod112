package carpet.helpers.lifetime;

import carpet.CarpetServer;
import carpet.helpers.lifetime.utils.GameUtil;
import carpet.utils.Messenger;
import net.minecraft.command.ICommandSender;

public abstract class AbstractTracker
{
    private final String name;
    private boolean tracking;
    private long startTick;
    private long startMillis;

    public AbstractTracker(String name)
    {
        this.name = name;
    }

    /*
     * ---------------------
     *  tracker name things
     * ---------------------
     */

    public String getName()
    {
        return this.name;
    }

    public String getCommandPrefix()
    {
        return this.name.toLowerCase();
    }

    // Xxx
    public String getTranslatedName()
    {
        return this.name;
    }

    // Xxx Tracker
    public String getTranslatedNameFull()
    {
        return String.format("%s Tracker", this.getTranslatedName());
    }

    /*
     * -----------------------
     *  status / info getters
     * -----------------------
     */

    public boolean isTracking()
    {
        return this.tracking;
    }

    public long getStartMillis()
    {
        return this.startMillis;
    }

    public long getStartTick()
    {
        return this.startTick;
    }

    /*
     * ------------------------
     *  for command executions
     * ------------------------
     */

    public int startTracking(ICommandSender source, boolean showFeedback)
    {
        if (this.isTracking())
        {
            if (showFeedback)
            {
                Messenger.m(source, Messenger.c(
                        "r " + String.format("%s is already running", this.getTranslatedNameFull())
                ));
            }
            return 1;
        }
        this.tracking = true;
        this.startTick = GameUtil.getGameTime();
        this.startMillis = System.currentTimeMillis();
        if (showFeedback)
        {
            Messenger.m(source, Messenger.c(
                    "w " + String.format("%s started", this.getTranslatedNameFull())
            ));
        }
        this.initTracker();
        return 1;
    }

    public int stopTracking(ICommandSender source, boolean showFeedback)
    {
        if (source != null)
        {
            if (this.isTracking())
            {
                this.reportTracking(source, false);
                if (showFeedback)
                {
                    Messenger.m(source, Messenger.c(
                            "w  \n",
                            "w " + String.format("%s stopped", this.getTranslatedNameFull())
                    ));
                }
            }
            else if (showFeedback)
            {
                Messenger.m(source, Messenger.c(
                        "r " + String.format("%s has not started", this.getTranslatedNameFull())
                ));
            }
        }
        this.tracking = false;
        return 1;
    }

    public int restartTracking(ICommandSender source)
    {
        boolean wasTracking = this.isTracking();
        this.stopTracking(source, false);
        this.startTracking(source, false);
        if (wasTracking)
        {
            source.sendMessage(Messenger.s(null, " "));
        }
        Messenger.m(source, Messenger.s(null, String.format("%s restarted", this.getTranslatedNameFull())));
        return 1;
    }

    protected int doWhenTracking(ICommandSender source, Runnable runnable)
    {
        if (this.isTracking())
        {
            runnable.run();
        }
        else
        {
            Messenger.m(source, Messenger.c(
                    "r " + String.format("%s has not started", this.getTranslatedNameFull())
            ));
        }
        return 1;
    }

    public int reportTracking(ICommandSender source, boolean realtime)
    {
        return this.doWhenTracking(source, () -> this.printTrackingResult(source, realtime));
    }

    /*
     * -------
     *  Utils
     * -------
     */

    protected long getTrackedTick(boolean realtime)
    {
        return Math.max(1, realtime ? (System.currentTimeMillis() - this.getStartMillis()) / 50 : GameUtil.getGameTime() - this.getStartTick());
    }

    // send general header for tracking report and return the processed "ticks"
    protected long sendTrackedTime(ICommandSender source, boolean realtime)
    {
        long ticks = this.getTrackedTick(realtime);
        source.sendMessage(Messenger.c(
                "w  \n",
                "g ----------- ",
                "w " + this.getTranslatedNameFull(),
                "g  -----------\n",
                String.format(
                        "w Tracked %.2f min (%s)",
                        (double)ticks / (20 * 60),
                        realtime ? "real time" : "in game"
                )
        ));
        return ticks;
    }

    /*
     * ------------
     *  Interfaces
     * ------------
     */

//    /**
//     * Stop tracking, call this when server stops
//     * e.g. inside {@link carpet.CarpetServer#onServerClosed}
//     */
//    public void stop()
//    {
//        this.stopTracking(null, false);
//    }

    /**
     * Called when the tracker starts tracking
     * Go initialize necessary statistics
     */
    protected abstract void initTracker();

    /**
     * Show tracking result to the command source
     * @param realtime use real time or not. if not, use in-game time
     */
    protected abstract void printTrackingResult(ICommandSender source, boolean realtime);
}
