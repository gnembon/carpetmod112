package carpet.mixin.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    public MobEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "playAmbientSound", at = @At("HEAD"), cancellable = true)
    private void easterEgg(CallbackInfo ci) {
        String name = Formatting.strip(getName());
        if ("Xcom".equalsIgnoreCase(name) || "gnembon".equalsIgnoreCase(name)) ci.cancel();
    }
}
