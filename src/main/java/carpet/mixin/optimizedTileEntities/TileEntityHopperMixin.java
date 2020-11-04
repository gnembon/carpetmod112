package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityOptimizer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TileEntityHopper.class)
public abstract class TileEntityHopperMixin implements TileEntityOptimizer.ILazyTileEntity {
    @Shadow protected abstract boolean isInventoryEmpty();
    @Shadow protected abstract boolean isFull();

    // CARPET-optimizedTileEntities: Whether the tile entity is asleep or not. Hoppers have 2 different actions that can sleep: pushing and pulling.
    // False by default so tile entities wake up upon chunk loading
    private boolean pullSleeping = false;
    private boolean pushSleeping = false;

    @Override
    public void wakeUp(){
        this.pullSleeping = false;
        this.pushSleeping = false;
    }

    @Redirect(method = "updateHopper", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryEmpty()Z"))
    private boolean isInventoryEmptyOrOptimized(TileEntityHopper hopper) {
        if (CarpetSettings.optimizedTileEntities) {
            pushSleeping = pushSleeping || isInventoryEmpty();
            return pushSleeping;
        }
        return isInventoryEmpty();
    }

    @Redirect(method = "updateHopper", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;isFull()Z"))
    private boolean isFullOrOptimized(TileEntityHopper hopper) {
        if (CarpetSettings.optimizedTileEntities) {
            pullSleeping = pullSleeping || isFull();
            return pullSleeping;
        }
        return isFull();
    }

    @Inject(method = "transferItemsOut", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryFull(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z")
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void setPushSleep(CallbackInfoReturnable<Boolean> cir, IInventory output) {
        // Push falls asleep if the container it would push into is full and
        // is an actual tile entity (not a minecart). This is because minecarts do not cause comparator updates and would keep the
        // hopper in a sleeping push state when leaving or emptying
        pushSleeping = output instanceof TileEntityLockable;
    }

    @Inject(method = "pullItems", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryEmpty(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z")
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void setPullSleep1(IHopper hopper, CallbackInfoReturnable<Boolean> cir, IInventory input) {
        // Pull falls asleep if the container it would pull from is empty and
        // is an actual tile entity (not a minecart). This is because minecarts do not cause comparator updates and would keep the
        // hopper in a sleeping pull state when leaving or filling up
        if (hopper instanceof TileEntityHopperMixin) {
            ((TileEntityHopperMixin) hopper).pullSleeping = input instanceof TileEntityLockable;
        }
    }

    @Inject(method = "pullItems", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void setPullSleep2(IHopper hopper, CallbackInfoReturnable<Boolean> cir) {
        // There is a non-empty inventory above the hopper, but for some reason the hopper cannot suck
        // items from it. Therefore the hopper pulling should sleep (if the inventory is not a minecart).
        if (hopper instanceof TileEntityHopperMixin) {
            ((TileEntityHopperMixin) hopper).pullSleeping = CarpetSettings.optimizedTileEntities && TileEntityHopper.getSourceInventory(hopper) instanceof TileEntityLockable;
        }
    }
}
