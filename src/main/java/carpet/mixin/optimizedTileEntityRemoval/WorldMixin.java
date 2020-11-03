package carpet.mixin.optimizedTileEntityRemoval;

import carpet.CarpetSettings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Mixin(World.class)
public class WorldMixin {
    @Shadow @Final private List<TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final public List<TileEntity> tickableTileEntities;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;

    @Inject(method = "updateEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tileEntitiesToBeRemoved:Ljava/util/List;", ordinal = 0))
    private void optimizedTileEntityRemoval(CallbackInfo ci) {
        if (!CarpetSettings.optimizedTileEntityRemoval || this.tileEntitiesToBeRemoved.isEmpty()) return;

        Set<TileEntity> remove = Collections.newSetFromMap(new IdentityHashMap<>());
        remove.addAll(this.tileEntitiesToBeRemoved);
        this.tickableTileEntities.removeAll(remove);
        this.loadedTileEntityList.removeAll(remove);
        this.tileEntitiesToBeRemoved.clear();
    }
}
