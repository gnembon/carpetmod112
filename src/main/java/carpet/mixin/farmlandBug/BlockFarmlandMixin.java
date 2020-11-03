package carpet.mixin.farmlandBug;

import carpet.CarpetSettings;
import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockFarmland.class)
public class BlockFarmlandMixin {
    @Shadow @Final protected static AxisAlignedBB FARMLAND_AABB;

    @Redirect(method = "turnToDirt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionAndUpdate(DDD)V"))
    private static void changeY(Entity entity, double x, double y, double z, World world, BlockPos pos) {
        if (CarpetSettings.farmlandBug) y = FARMLAND_AABB.offset(pos).maxY;
        entity.setPositionAndUpdate(x, y, z);
    }
}
