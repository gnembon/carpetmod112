package carpet.mixin.duplicationFixGravityBlock;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityFallingBlock.class, priority = 1001)
public abstract class EntityFallingBlockMixin extends Entity {
    public EntityFallingBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityFallingBlock;move(Lnet/minecraft/entity/MoverType;DDD)V", shift = At.Shift.AFTER), cancellable = true)
    private void dupeFix(CallbackInfo ci) {
        //Fix falling blocks duplication when going through portals. CARPET-XCOM
        if (CarpetSettings.duplicationFixGravityBlocks && isDead) ci.cancel();
    }
}
