package carpet.mixin.accessors;

import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.WoodlandMansion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGeneratorOverworld.class)
public interface ChunkGeneratorOverworldAccessor {
    @Accessor WoodlandMansion getWoodlandMansionGenerator();
}
