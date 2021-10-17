package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

public class ExplosionLogHelper {

    // CARPET-SYLKOS
    // Some code yeeted from lntricarpet and gnembon 1.16+ fabric carpet

    public final Vec3d pos;
    public final Entity entity;
    private static boolean affectBlocks = false; // will be used later when I add in the full explosion logger
    private static long lastGametime = 0;
    private static long explosionCountInCurrentGT = 0;
    private static long explosionCountInCurrentPos = 0;
    public static Vec3d previousPosition = null;
    public static long startTime = 0;

    public static boolean tickHasCompact = false;

    public ExplosionLogHelper(Entity entity, double x, double y, double z, float power, boolean createFire) { // blocks removed
        this.entity = entity;
        this.pos = new Vec3d(x, y, z);
    }

    public void onExplosionDone(long gametime) {
        if(lastGametime != gametime) {
            explosionCountInCurrentGT = 1;
            explosionCountInCurrentPos = 0;
            previousPosition = pos;
            lastGametime = gametime;
            startTime = System.currentTimeMillis();
            LoggerRegistry.getLogger("explosions").log((option) -> {
                return new ITextComponent[]{Messenger.m(null, "wb tick : ", "d " + gametime)};
            });
        }

        LoggerRegistry.getLogger("explosions").log((option) -> {
            ITextComponent[] msg = null;
            switch (option) {
                case "brief":
                    msg = new ITextComponent[]{Messenger.m(null,
                            "d #" + explosionCountInCurrentGT,
                            "gb ->",
                            Messenger.dblt("l", pos.x, pos.y, pos.z),
                            (affectBlocks)?"m (affects blocks)":"m  (doesn't affect blocks)"
                            )};
                    explosionCountInCurrentGT++;
                    break;

                // temporarily removed "full" because its not really needed for my use case. may implement later - Sylkos

                case "compact":
                    tickHasCompact = true;
                    if(previousPosition != null && !pos.equals(previousPosition))  {
                        msg = new ITextComponent[]{Messenger.m(null,
                                "d #" + explosionCountInCurrentGT,
                                "gb ->",
                                "d " + explosionCountInCurrentPos + "x ",
                                Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z),
                                (affectBlocks)?"m (affects blocks)":"m  (doesn't affect blocks)",
                                "g (", "d " + (System.currentTimeMillis()-startTime), "g ms)"
                                )};
                        explosionCountInCurrentGT += explosionCountInCurrentPos;
                        explosionCountInCurrentPos = 0;
                        previousPosition = pos;
                        startTime = System.currentTimeMillis();
                    }
                    explosionCountInCurrentPos++;
                    break;
            }
            return msg;
        });
    }

    public static void logLastExplosion() {
        if(LoggerRegistry.__explosions) {
            if (tickHasCompact) {
                tickHasCompact = false;
                LoggerRegistry.getLogger("explosions").log((option) -> {
                    ITextComponent[] msg = null;
                    if ("compact".equals(option)) {
                        if (previousPosition != null) {
                            msg = new ITextComponent[]{Messenger.m(null,
                                    "d #" + (explosionCountInCurrentGT),
                                    "gb ->",
                                    "d " + explosionCountInCurrentPos + "x ",
                                    Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z),
                                    (affectBlocks) ? "m (affects blocks)" : "m  (doesn't affect blocks)",
                                    "g (", "d " + (System.currentTimeMillis()-startTime), "g ms)"
                                    )};
                        }
                        startTime = 0;
                    }
                    return msg;
                });
            }
        }
    }
}
