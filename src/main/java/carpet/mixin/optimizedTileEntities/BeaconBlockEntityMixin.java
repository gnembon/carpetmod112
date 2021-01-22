package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.block.entity.AbstractContainerBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends AbstractContainerBlockEntity {
    // If optimized and it canSeeSky, skip segment color calculations server-side by increasing the initial value of the loop counter
    @ModifyConstant(method = "method_26900", constant = @Constant(intValue = 1, ordinal = 2))
    private int optimizedTileEntitiesOffset(int origValue) {
        if (!CarpetSettings.optimizedTileEntities || world.isClient) return origValue;
        return world.dimension.hasSkyLight() && world.isSkyVisible(pos) ? 256 : origValue;
    }
}
