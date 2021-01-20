package carpet.mixin.duplicationFixGravityBlock;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FallingBlockEntity.class, priority = 1001)
public abstract class FallingBlockEntityMixin extends Entity {
    public FallingBlockEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/FallingBlockEntity;method_34411(Lnet/minecraft/entity/MovementType;DDD)V", shift = At.Shift.AFTER), cancellable = true)
    private void dupeFix(CallbackInfo ci) {
        //Fix falling blocks duplication when going through portals. CARPET-XCOM
        if (CarpetSettings.duplicationFixGravityBlocks && removed) ci.cancel();
    }
}
