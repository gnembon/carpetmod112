package carpet.mixin.loggers;

import carpet.utils.extensions.ExtendedTileEntityComparator;
import net.minecraft.tileentity.TileEntityComparator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TileEntityComparator.class)
public class TileEntityComparatorMixin implements ExtendedTileEntityComparator {
    // CM: instant comparator logger, stored in world time modulo 3.
    // This is to allow for further tile tick scheduling in the same tick before the tile tick is processed
    private final int[] scheduledOutputSignal = new int[3];
    private final boolean[] buggy = new boolean[3];

    @Override
    public int[] getScheduledOutputSignal() {
        return scheduledOutputSignal;
    }

    @Override
    public boolean[] getBuggy() {
        return buggy;
    }
}
