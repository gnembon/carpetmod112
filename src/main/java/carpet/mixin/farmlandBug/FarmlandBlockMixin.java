package carpet.mixin.farmlandBug;

import carpet.CarpetSettings;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @Shadow @Final protected static Box field_24385;

    @Redirect(method = "method_26609", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;requestTeleport(DDD)V"))
    private static void changeY(Entity entity, double x, double y, double z, World world, BlockPos pos) {
        if (CarpetSettings.farmlandBug) y = field_24385.offset(pos).y2;
        entity.requestTeleport(x, y, z);
    }
}
