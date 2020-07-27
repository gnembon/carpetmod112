package carpet.mixin.villageMarkers;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.ExtendedVillageCollection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillageCollection.class)
public class VillageCollectionMixin implements ExtendedVillageCollection {
    @Shadow private World world;
    private boolean updateMarkers;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/VillageCollection;addNewDoorsToVillageOrCreateVillage()V", shift = At.Shift.AFTER))
    private void updateMarkers(CallbackInfo ci) {
        if (updateMarkers) {
            CarpetClientMarkers.updateClientVillageMarkers(world);
            updateMarkers = false;
        }
    }

    @Inject(method = "removeAnnihilatedVillages", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/VillageCollection;markDirty()V"))
    private void updateOnRemove(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(method = "addNewDoorsToVillageOrCreateVillage", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/Village;addVillageDoorInfo(Lnet/minecraft/village/VillageDoorInfo;)V"))
    private void updateOnAdd(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(method = "readFromNBT", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private void updateOnDeserialize(NBTTagCompound nbt, CallbackInfo ci) {
        updateMarkers = true;
    }

    @Override
    public void markVillageMarkersDirty() {
        updateMarkers = true;
    }
}
