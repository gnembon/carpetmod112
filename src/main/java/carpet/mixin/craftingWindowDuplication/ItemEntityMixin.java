package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I", shift = At.Shift.AFTER))
    private void dupeItemScanStart(PlayerEntity entityIn, CallbackInfo ci) {
        if(CarpetSettings.craftingWindowDuplication && entityIn instanceof DupingPlayer){
            ((DupingPlayer)entityIn).dupeItemScan(true);
        }
    }

    @Inject(method = "onPlayerCollision", at = @At("RETURN"))
    private void dupeItemScanEnd(PlayerEntity entityIn, CallbackInfo ci) {
        if (world.isClient) return;
        if(CarpetSettings.craftingWindowDuplication && entityIn instanceof DupingPlayer){
            ((DupingPlayer)entityIn).dupeItemScan(false);
        }
    }
}
