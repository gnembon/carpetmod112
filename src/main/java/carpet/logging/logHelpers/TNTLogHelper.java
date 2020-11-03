package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class TNTLogHelper
{
    private double primedX, primedY, primedZ, primedAngle;
    /**
     * Runs when the TNT is primed. Expects the position and motion angle of the TNT.
     */
    public void onPrimed(double x, double y, double z, float angle)
    {
        primedX = x;
        primedY = y;
        primedZ = z;
        primedAngle = angle;
    }
    /**
     * Runs when the TNT explodes. Expects the position of the TNT.
     */
    public void onExploded(double x, double y, double z)
    {
        LoggerRegistry.getLogger("tnt").log( (option) -> {
            switch (option)
            {
                case "brief":
                    return new ITextComponent[]{Messenger.m(null,
                            "l P ",Messenger.dblt("l",primedX,primedY,primedZ,primedAngle),
                            "r  E ",Messenger.dblt("r",x, y, z))};
                case "full":
                    return new ITextComponent[]{Messenger.m(null, "l P ",Messenger.dblf("l",primedX,primedY,primedZ,primedAngle),
                            "r  E ",Messenger.dblf("r",x, y, z))};
            }
            return null;
        },
        "PRIMED_X", primedX,
        "PRIMED_Y", primedY,
        "PRIMED_Z", primedZ,
        "PRIMED_ANGLE", primedAngle,
        "EXPLODE_X", x,
        "EXPLODE_Y", y,
        "EXPLODE_Z", z);
    }

}
