package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TileEntityBeacon.class)
public abstract class TileEntityBeaconMixin extends TileEntityLockable {
    // If optimized and it canSeeSky, skip segment color calculations server-side by increasing the initial value of the loop counter
    @ModifyConstant(method = "updateSegmentColors", constant = @Constant(intValue = 1, ordinal = 2))
    private int optimizedTileEntitiesOffset(int origValue) {
        if (!CarpetSettings.optimizedTileEntities || world.isRemote) return origValue;
        return world.provider.hasSkyLight() && world.canSeeSky(pos) ? 256 : origValue;
    }
}
