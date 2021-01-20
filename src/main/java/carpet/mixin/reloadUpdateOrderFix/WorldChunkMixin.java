package carpet.mixin.reloadUpdateOrderFix;

import carpet.CarpetSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Redirect(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private HashMap<BlockPos, BlockEntity> newTileEntityMap() {
        return CarpetSettings.reloadUpdateOrderFix ? new LinkedHashMap<>() : new HashMap<>();
    }
}
