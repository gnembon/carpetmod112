package carpet.mixin.hopperCounters;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.HopperCounter;
import carpet.utils.WoolTool;
import net.minecraft.block.BlockHopper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityHopper.class)
public abstract class TileEntityHopperMixin extends TileEntityLockableLoot {
    @Shadow public abstract int getSizeInventory();
    @Shadow public abstract double getXPos();
    @Shadow public abstract double getYPos();
    @Shadow public abstract double getZPos();

    @Inject(method = "transferItemsOut", at = @At("HEAD"), cancellable = true)
    private void onPush(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.hopperCounters != CarpetSettings.HopperCounters.off) {
            String counter = getCounterName();
            if (counter != null) {
                for (int i = 0; i < this.getSizeInventory(); ++i) {
                    if (!this.getStackInSlot(i).isEmpty()) {
                        ItemStack itemstack = this.getStackInSlot(i);//.copy();
                        HopperCounter.COUNTERS.get(counter).add(this.getWorld().getMinecraftServer(), itemstack);
                        this.setInventorySlotContents(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private String getCounterName() {
        if (CarpetSettings.hopperCounters == CarpetSettings.HopperCounters.all) return "all";
        BlockPos woolPos = new BlockPos(getXPos(), getYPos(), getZPos()).offset(BlockHopper.getFacing(this.getBlockMetadata()));
        CarpetClientChunkLogger.setReason("Hopper loading");
        EnumDyeColor wool_color = WoolTool.getWoolColorAtPosition(getWorld(), woolPos);
        CarpetClientChunkLogger.resetToOldReason();
        return wool_color.getName();
    }
}
