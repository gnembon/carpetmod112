package carpet.mixin.worldEdit;

import carpet.helpers.CapturedDrops;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public class BlockMixin {
    @Redirect(method = "spawnAsEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private static boolean captureDrops(World world, Entity entity) {
        if (world.spawnEntity(entity)) {
            if (CapturedDrops.isCapturingDrops()) CapturedDrops.captureDrop((EntityItem) entity);
            return true;
        }
        return false;
    }
}
