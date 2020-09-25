package carpet.mixin.dimensionLoading;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(WorldServer.class)
public class WorldServerMixin {
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
    private boolean isPlayerListEmpty(List<EntityPlayerMP> list) {
        return !CarpetSettings.dimensionLoading && list.isEmpty();
    }
}
