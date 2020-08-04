package carpet.mixin.core;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin extends EntityLivingBase {
    public EntityLivingMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "playLivingSound", at = @At("HEAD"), cancellable = true)
    private void easterEgg(CallbackInfo ci) {
        String name = TextFormatting.getTextWithoutFormattingCodes(getName());
        if ("Xcom".equalsIgnoreCase(name) || "gnembon".equalsIgnoreCase(name)) ci.cancel();
    }
}
