package carpet.logging.logHelpers;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic log helper for logging the trajectory of things like blocks and throwables.
 */
public class TrajectoryLogHelper
{
    private static final int MAX_TICKS_PER_LINE = 20;

    private boolean doLog;
    private Logger logger;

    private ArrayList<Vec3d> positions = new ArrayList<>();
    private ArrayList<Vec3d> motions = new ArrayList<>();
    private int sentLogs;

    public TrajectoryLogHelper(String logName)
    {
        this.logger = LoggerRegistry.getLogger(logName);
        this.doLog = this.logger.hasSubscribers();
        sentLogs = 0;
    }

    public void onTick(double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        if (!doLog) return;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(motionX, motionY, motionZ));
        sendUpdateLogs(false);
    }

    public void onFinish()
    {
        if (!doLog) return;
        sentLogs = 0;
        sendUpdateLogs(true);
        doLog = false;
    }

    private void sendUpdateLogs(boolean finished) {
        logger.logNoCommand( (option) -> {
            List<ITextComponent> comp = new ArrayList<>();
            switch (option)
            {
                case "brief":
                    if(!finished)  return null;
                    return finalReport(comp).toArray(new ITextComponent[0]);
                case "full":
                    if(finished) return finalReport(comp).toArray(new ITextComponent[0]);
                    for (int i = sentLogs; i < positions.size(); i++)
                    {
                        sentLogs++;
                        Vec3d pos = positions.get(i);
                        Vec3d mot = motions.get(i);
                        comp.add(Messenger.m(null ,
                                String.format("w tick: %d pos",i),Messenger.dblt("w",pos.x, pos.y, pos.z),
                                "w   mot",Messenger.dblt("w",mot.x, mot.y, mot.z), Messenger.m(null, "w  [tp]", "/tp " + pos.x +" "+ pos.y +" "+ pos.z) ));
                    }
                    break;
            }
            return comp.toArray(new ITextComponent[0]);
        });
    }

    private List<ITextComponent> finalReport(List<ITextComponent> comp){
        comp.add(Messenger.m(null,"w ---------"));
        List<String> line = new ArrayList<>();
        for (int i = sentLogs; i < positions.size(); i++)
        {
            sentLogs++;
            Vec3d pos = positions.get(i);
            Vec3d mot = motions.get(i);
            line.add("w  x");
            line.add(String.format("^w Tick: %d\nx: %f\ny: %f\nz: %f\n------------\nmx: %f\nmy: %f\nmz: %f",
                    i, pos.x, pos.y, pos.z, mot.x, mot.y, mot.z));
            line.add("?/tp " + pos.x +" "+ pos.y +" "+ pos.z);
            if ((((i+1) % MAX_TICKS_PER_LINE)==0) || i == positions.size()-1)
            {
                comp.add(Messenger.m(null, line.toArray(new Object[0])));
                line.clear();
            }
        }
        return comp;
    }
}