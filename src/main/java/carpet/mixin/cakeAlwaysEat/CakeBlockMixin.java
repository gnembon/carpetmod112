package carpet.mixin.cakeAlwaysEat;

import carpet.CarpetSettings;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
    @Redirect(method = "tryEat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canConsume(Z)Z"))
    private boolean canEat(PlayerEntity player, boolean ignoreHunger) {
        return CarpetSettings.cakeAlwaysEat || player.canConsume(ignoreHunger);
    }
}
