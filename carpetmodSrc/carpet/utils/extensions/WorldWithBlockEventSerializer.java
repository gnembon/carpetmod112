package carpet.utils.extensions;

import carpet.helpers.ScheduledBlockEventSerializer;

public interface WorldWithBlockEventSerializer {
    ScheduledBlockEventSerializer getBlockEventSerializer();
}
