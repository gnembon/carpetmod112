package carpet.mixin.duplicationFixItemFrame;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityItemFrame.class)
public abstract class EntityItemFrameMixin extends EntityHanging {
    public EntityItemFrameMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "dropItemOrSelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private boolean checkDead(GameRules gameRules, String name) {
        return gameRules.getBoolean(name) && (!CarpetSettings.duplicationFixItemFrame || !isDead);
    }
}
