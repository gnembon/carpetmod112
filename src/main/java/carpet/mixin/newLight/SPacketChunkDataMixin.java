package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SPacketChunkData.class)
public class SPacketChunkDataMixin {
    // Because Mixin doesn't allow @Injects before return
    @Redirect(method = "<init>(Lnet/minecraft/world/chunk/Chunk;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;provider:Lnet/minecraft/world/WorldProvider;"))
    private WorldProvider getProviderAndProcLight(World world) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates();
        return world.provider;
    }
}
