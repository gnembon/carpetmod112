package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import carpet.mixin.accessors.ItemEntityAccessor;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();
    @Shadow private int pickupDelay;
    @Shadow private int age;

    public ItemEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean insertStack(PlayerInventory inventory, ItemStack stack) {
        try {
            CarpetServer.playerInventoryStacking.set(Boolean.TRUE);
            return inventory.insertStack(stack);
        } finally {
            CarpetServer.playerInventoryStacking.set(Boolean.FALSE);
        }
    }

    @Inject(method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)Z", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    ), cancellable = true)
    private void doGroundStacking(ItemEntity other, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ownStack = this.getStack();
        ItemStack otherStack = other.getStack();
        // Add check for stacking shoulkers without NBT on the ground CARPET-XCOM
        if (((ExtendedItemStack) (Object) otherStack).isGroundStackable() && ((ExtendedItemStack) (Object) ownStack).isGroundStackable()) {
            otherStack.increment(ownStack.getCount());
            ((ItemEntityAccessor) other).setPickupDelay(Math.max(((ItemEntityAccessor) other).getPickupDelay(), this.pickupDelay));
            ((ItemEntityAccessor) other).setAge(Math.min(((ItemEntityAccessor) other).getAge(), this.age));
            other.setStack(otherStack);
            this.remove();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;increment(I)V"), cancellable = true)
    private void checkStackable(ItemEntity other, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ownStack = this.getStack();
        ItemStack otherStack = other.getStack();
        // make sure stackable items are checked before combining them, always true in vanilla CARPET-XCOM
        if (!ownStack.isStackable() || !otherStack.isStackable()) {
            cir.setReturnValue(false);
        }
    }
}
