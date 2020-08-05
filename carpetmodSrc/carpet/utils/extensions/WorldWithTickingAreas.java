package carpet.utils.extensions;

import carpet.utils.TickingArea;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

public interface WorldWithTickingAreas {
    List<TickingArea> getTickingAreas();

    LongSet getTickingChunks();
}
