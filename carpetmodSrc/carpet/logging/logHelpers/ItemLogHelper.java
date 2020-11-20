package carpet.logging.logHelpers;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ItemLogHelper {
    private boolean doLog;
    private Logger logger;

    private ArrayList<Vec3d> positions = new ArrayList<>();
    private ArrayList<Vec3d> motions = new ArrayList<>();
    private int sentLogs;

    public static long seedWorld = 0;
    public static long seedMathRandom = 0;

    public ItemLogHelper(String logName, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        this.logger = LoggerRegistry.getLogger(logName);
        this.doLog = this.logger.hasSubscribers();
        sentLogs = 0;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(motionX, motionY, motionZ));
    }

    public void onTick(double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        if (!doLog) return;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(motionX, motionY, motionZ));
    }

    public void onFinish(String type)
    {
        if (!doLog) return;
        sentLogs = 0;
        sendUpdateLogs(true, type);
        doLog = false;
    }

    private void sendUpdateLogs(boolean finished, String type) {
        logger.logNoCommand( (option) -> {
            List<ITextComponent> comp = new ArrayList<>();
            switch (option)
            {
                case "brief":
                    Vec3d p = new Vec3d(0,0,0);
                    if(positions.size() > 0) {
                        p = positions.get(positions.size() - 1);
                    }
                    comp.add(Messenger.m(null,"","w ----" + type + "---- t: " + positions.size() + "  pos: ", Messenger.dblt("w",p.x, p.y, p.z)));
                    return comp.toArray(new ITextComponent[0]);
                case "full":
                    comp.add(Messenger.m(null,"w ----" + type + "---- t: " + positions.size()));
                    comp.add(Messenger.m(null,Messenger.m(null, "w world: " + seedWorld, "^w " + seedWorld, "?" + seedWorld), Messenger.m(null, "w    math: " + seedMathRandom, "^w " + seedMathRandom, "?" + seedMathRandom)));
                    for (int i = sentLogs; i < positions.size(); i++)
                    {
                        Vec3d pos = positions.get(i);
                        Vec3d mot = motions.get(i);
                        comp.add(Messenger.m(null ,
                                String.format("w tick: %d pos",(i+1)),Messenger.dblt("w",pos.x, pos.y, pos.z),
                                "w   mot",Messenger.dblt("w",mot.x, mot.y, mot.z), Messenger.m(null, "w  [tp]", "/tp " + pos.x +" "+ pos.y +" "+ pos.z) ));
                        sentLogs++;
                    }
                    break;
            }
            return comp.toArray(new ITextComponent[0]);
        });
    }

    public static long getMathRandomSeed() {
        try {
            Field field = Random.class.getDeclaredField("seed");
            field.setAccessible(true);
            return ((AtomicLong) field.get(getMathRandom())).get();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static void setMathRandomSeed(long set) {
        try {
            Field field = Random.class.getDeclaredField("seed");
            field.setAccessible(true);
            ((AtomicLong) field.get(getMathRandom())).set(set);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static Random getMathRandom() {
        try {
            Class<?> clazz = Class.forName("java.lang.Math$RandomNumberGeneratorHolder");
            Field field = clazz.getDeclaredField("randomNumberGenerator");
            field.setAccessible(true);
            return (Random) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
