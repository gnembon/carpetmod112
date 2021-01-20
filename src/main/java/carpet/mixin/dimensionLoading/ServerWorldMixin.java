package carpet.mixin.dimensionLoading;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
    private boolean isPlayerListEmpty(List<ServerPlayerEntity> list) {
        return !CarpetSettings.dimensionLoading && list.isEmpty();
    }
}
