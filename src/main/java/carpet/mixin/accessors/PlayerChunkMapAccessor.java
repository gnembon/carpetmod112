package carpet.mixin.accessors;

import net.minecraft.class_4615;
import net.minecraft.class_6380;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(class_6380.class)
public interface PlayerChunkMapAccessor {
    @Accessor("field_31809") List<class_4615> getEntries();
    @Accessor("field_31808") List<class_4615> getEntriesWithoutChunks();
}
