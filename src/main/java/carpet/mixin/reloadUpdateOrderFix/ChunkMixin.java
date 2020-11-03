package carpet.mixin.reloadUpdateOrderFix;

import carpet.CarpetSettings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(Chunk.class)
public class ChunkMixin {
    @Redirect(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private HashMap<BlockPos, TileEntity> newTileEntityMap() {
        return CarpetSettings.reloadUpdateOrderFix ? new LinkedHashMap<>() : new HashMap<>();
    }
}
