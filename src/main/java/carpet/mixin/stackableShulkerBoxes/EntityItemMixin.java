package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetServer;
import carpet.mixin.accessors.EntityItemAccessor;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin extends Entity {
    @Shadow public abstract ItemStack getItem();
    @Shadow private int pickupDelay;
    @Shadow private int age;

    public EntityItemMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean addItemStackToInventory(InventoryPlayer inventory, ItemStack stack) {
        try {
            CarpetServer.playerInventoryStacking.set(Boolean.TRUE);
            return inventory.addItemStackToInventory(stack);
        } finally {
            CarpetServer.playerInventoryStacking.set(Boolean.FALSE);
        }
    }

    @Inject(method = "combineItems", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxStackSize()I")
    ), cancellable = true)
    private void doGroundStacking(EntityItem other, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ownStack = this.getItem();
        ItemStack otherStack = other.getItem();
        // Add check for stacking shoulkers without NBT on the ground CARPET-XCOM
        if (((ExtendedItemStack) (Object) otherStack).isGroundStackable() && ((ExtendedItemStack) (Object) ownStack).isGroundStackable()) {
            otherStack.grow(ownStack.getCount());
            ((EntityItemAccessor) other).setPickupDelay(Math.max(((EntityItemAccessor) other).getPickupDelay(), this.pickupDelay));
            ((EntityItemAccessor) other).setAge(Math.min(((EntityItemAccessor) other).getAge(), this.age));
            other.setItem(otherStack);
            this.setDead();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "combineItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V"), cancellable = true)
    private void checkStackable(EntityItem other, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ownStack = this.getItem();
        ItemStack otherStack = other.getItem();
        // make sure stackable items are checked before combining them, always true in vanilla CARPET-XCOM
        if (!ownStack.isStackable() || !otherStack.isStackable()) {
            cir.setReturnValue(false);
        }
    }
}
