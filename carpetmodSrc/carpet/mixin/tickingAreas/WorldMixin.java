package carpet.mixin.tickingAreas;

import carpet.utils.TickingArea;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(World.class)
public class WorldMixin implements carpet.utils.extensions.WorldWithTickingAreas {
    private final List<TickingArea> tickingAreas = new ArrayList<>();
    private final LongSet tickingChunks = new LongOpenHashSet();

    @Override
    public List<TickingArea> getTickingAreas() {
        return tickingAreas;
    }

    @Override
    public LongSet getTickingChunks() {
        return tickingChunks;
    }
}
