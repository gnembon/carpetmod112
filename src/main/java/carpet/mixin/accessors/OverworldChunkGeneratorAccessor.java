package carpet.mixin.accessors;

import net.minecraft.world.gen.chunk.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.WoodlandMansionFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OverworldChunkGenerator.class)
public interface OverworldChunkGeneratorAccessor {
    @Accessor WoodlandMansionFeature getWoodlandMansionFeature();
}
