package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import net.minecraft.class_4615;
import net.minecraft.class_6380;
import net.minecraft.server.world.ServerChunkCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow public abstract ServerChunkCache getChunkManager();

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33579(II)Z"))
    private boolean isInPlayerChunkMap(class_6380 map, int chunkX, int chunkZ) {
        class_4615 entry = map.method_33587(chunkX, chunkZ);
        if (entry != null && CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            Chunk chunk = entry.method_33575();
            getChunkManager().method_33448(chunk);
            chunk.field_25367 = false;
            return true;
        }
        return false;
    }
}
