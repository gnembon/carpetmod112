package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class InstantComparators {

    public static void onNoTileEntity(BlockPos pos) {
        if (LoggerRegistry.__instantComparators) {
            LoggerRegistry.getLogger("instantComparators").log(option -> new ITextComponent[] {
                        Messenger.m(null, "y Comparator has no tile entity ", Messenger.tp("y", pos))
                },
            "X", pos.getX(),
            "Y", pos.getY(),
            "Z", pos.getZ());
        }
    }

    public static void onInstantComparator(BlockPos pos, boolean buggy) {
        if (LoggerRegistry.__instantComparators) {
            LoggerRegistry.getLogger("instantComparators").log(option -> {
                if (buggy ? !"tileTick".equals(option) : !"buggy".equals(option)) {
                    return new ITextComponent[] {
                            Messenger.m(null, "l " + (buggy ? "Buggy" : "Tile tick") + " instant comparator detected ", Messenger.tp("y", pos))
                    };
                } else {
                    return null;
                }
            },
            "X", pos.getX(),
            "Y", pos.getY(),
            "Z", pos.getZ(),
            "BUGGY", buggy);
        }
    }

}
