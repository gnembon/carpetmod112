package carpet.mixin.villageMarkers;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.ExtendedVillageCollection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import net.minecraft.world.storage.VillageState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillageState.class)
public class VillageStateMixin implements ExtendedVillageCollection {
    @Shadow private World field_33667;
    private boolean updateMarkers;

    @Inject(method = "method_35113", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/VillageState;method_35127()V", shift = At.Shift.AFTER))
    private void updateMarkers(CallbackInfo ci) {
        if (updateMarkers) {
            CarpetClientMarkers.updateClientVillageMarkers(field_33667);
            updateMarkers = false;
        }
    }

    @Inject(method = "method_35123", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/VillageState;markDirty()V"))
    private void updateOnRemove(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(method = "method_35127", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2792;method_35086(Lnet/minecraft/class_4209;)V"))
    private void updateOnAdd(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(method = "fromTag", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private void updateOnDeserialize(CompoundTag nbt, CallbackInfo ci) {
        updateMarkers = true;
    }

    @Override
    public void markVillageMarkersDirty() {
        updateMarkers = true;
    }
}
