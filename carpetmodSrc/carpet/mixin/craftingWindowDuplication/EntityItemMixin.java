package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin extends Entity {
    public EntityItemMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I", shift = At.Shift.AFTER))
    private void dupeItemScanStart(EntityPlayer entityIn, CallbackInfo ci) {
        if(CarpetSettings.craftingWindowDuplication && entityIn instanceof DupingPlayer){
            ((DupingPlayer)entityIn).dupeItemScan(true);
        }
    }

    @Inject(method = "onCollideWithPlayer", at = @At("RETURN"))
    private void dupeItemScanEnd(EntityPlayer entityIn, CallbackInfo ci) {
        if (world.isRemote) return;
        if(CarpetSettings.craftingWindowDuplication && entityIn instanceof DupingPlayer){
            ((DupingPlayer)entityIn).dupeItemScan(false);
        }
    }
}
