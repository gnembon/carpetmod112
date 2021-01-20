package carpet.mixin.hopperCounters;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.HopperCounter;
import carpet.utils.WoolTool;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    @Shadow public abstract int getInvSize();
    @Shadow public abstract double getHopperX();
    @Shadow public abstract double getHopperY();
    @Shadow public abstract double getHopperZ();

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onPush(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.hopperCounters != CarpetSettings.HopperCounters.off) {
            String counter = getCounterName();
            if (counter != null) {
                for (int i = 0; i < this.getInvSize(); ++i) {
                    if (!this.getInvStack(i).isEmpty()) {
                        ItemStack itemstack = this.getInvStack(i);//.copy();
                        HopperCounter.COUNTERS.get(counter).add(this.getWorld().getServer(), itemstack);
                        this.setInvStack(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private String getCounterName() {
        if (CarpetSettings.hopperCounters == CarpetSettings.HopperCounters.all) return "all";
        BlockPos woolPos = new BlockPos(getHopperX(), getHopperY(), getHopperZ()).offset(HopperBlock.method_26653(this.method_26938()));
        CarpetClientChunkLogger.setReason("Hopper loading");
        DyeColor wool_color = WoolTool.getWoolColorAtPosition(getWorld(), woolPos);
        CarpetClientChunkLogger.resetToOldReason();
        return wool_color.asString();
    }
}
