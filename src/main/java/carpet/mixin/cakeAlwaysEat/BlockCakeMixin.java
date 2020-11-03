package carpet.mixin.cakeAlwaysEat;

import carpet.CarpetSettings;
import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCake.class)
public class BlockCakeMixin {
    @Redirect(method = "eatCake", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;canEat(Z)Z"))
    private boolean canEat(EntityPlayer player, boolean ignoreHunger) {
        return CarpetSettings.cakeAlwaysEat || player.canEat(ignoreHunger);
    }
}
