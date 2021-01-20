package carpet.mixin.randomTickIndexing;

import carpet.carpetclient.CarpetClientRandomtickingIndexing;
import net.minecraft.class_6380;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_6380.class)
public class PlayerChunkMapMixin {
    @Shadow @Final private ServerWorld field_31803;

    @Inject(method = "method_33590", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        // Sends updates to all subscribed players that want to get indexing of chunks Carpet-XCOM
        if (CarpetClientRandomtickingIndexing.sendUpdates(field_31803)) {
            CarpetClientRandomtickingIndexing.sendRandomtickingChunkOrder(field_31803, (class_6380) (Object) this);
        }
    }
}
