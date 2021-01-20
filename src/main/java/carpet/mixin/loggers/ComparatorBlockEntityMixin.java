package carpet.mixin.loggers;

import carpet.utils.extensions.ExtendedComparatorBlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComparatorBlockEntity.class)
public class ComparatorBlockEntityMixin implements ExtendedComparatorBlockEntity {
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
