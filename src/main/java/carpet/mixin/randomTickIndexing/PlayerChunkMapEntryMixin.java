package carpet.mixin.randomTickIndexing;

import carpet.carpetclient.CarpetClientRandomtickingIndexing;
import net.minecraft.class_4615;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4615.class)
public class PlayerChunkMapEntryMixin {
    @Inject(method = "method_33566", at = @At("RETURN"))
    private void onAdd(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientRandomtickingIndexing.enableUpdate(player);
    }

    @Inject(method = "method_33569", at = @At("RETURN"))
    private void onRemove(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientRandomtickingIndexing.enableUpdate(player);
    }
}
