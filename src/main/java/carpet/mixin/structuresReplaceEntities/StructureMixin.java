package carpet.mixin.structuresReplaceEntities;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Structure.class)
public abstract class StructureMixin {
    @Shadow private BlockPos size;

    @Shadow private static Vec3d method_28030(Vec3d vec, BlockMirror mirrorIn, BlockRotation rotationIn) { throw new AbstractMethodError(); }

    @Inject(method = "method_28021", at = @At("HEAD"))
    private void replaceEntities(World worldIn, BlockPos pos, BlockMirror mirrorIn, BlockRotation rotationIn, BlockBox aabb, CallbackInfo ci) {
        if (!CarpetSettings.structuresReplaceEntities) return;
        Box box = new Box(pos, new BlockPos(method_28030((new Vec3d(pos.add(size))), mirrorIn, rotationIn)));
        for (Entity e : worldIn.getEntities(Entity.class, box, entity -> !(entity instanceof PlayerEntity))) {
            System.out.println(e);
            e.kill();
        }
    }
}
