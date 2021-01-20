package carpet.mixin.fastMovingEntityOptimization;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;
    @Shadow public abstract Box getBoundingBox();

    private boolean optimize;

    @Redirect(method = "method_34411", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26047(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;", ordinal = 3))
    private List<Box> fastMovingEntityOptimization(World world, Entity entity, Box box, MovementType type, double x, double y, double z) {
        if (CarpetSettings.fastMovingEntityOptimization &&
                (x > 4 || x < -4 ||
                y > 4 || y < -4 ||
                z > 4 || z < -4)) {
            optimize = true;
            return new ArrayList<>();
        } else {
            optimize = false;
            return world.method_26047(entity, box);
        }
    }

    @Inject(method = "method_34411", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fastMovingEntityOptimizationY(MovementType type, double x, double y, double z, CallbackInfo ci, double d10, double d11, double d1, double d2, double d3, double d4, List<Box> list1) {
        if (optimize) {
            list1.addAll(this.world.method_26047((Entity) (Object) this, this.getBoundingBox().stretch(0, y, 0)));
        }
    }

    @Inject(method = "method_34411", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fastMovingEntityOptimizationX(MovementType type, double x, double y, double z, CallbackInfo ci, double d10, double d11, double d1, double d2, double d3, double d4, List<Box> list1) {
        if (optimize) {
            list1.clear();
            list1.addAll(this.world.method_26047((Entity) (Object) this, this.getBoundingBox().stretch(x, 0, 0)));
        }
    }

    @Inject(method = "method_34411", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false, ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fastMovingEntityOptimizationZ(MovementType type, double x, double y, double z, CallbackInfo ci, double d10, double d11, double d1, double d2, double d3, double d4, List<Box> list1) {
        if (optimize) {
            list1.clear();
            list1.addAll(this.world.method_26047((Entity) (Object) this, this.getBoundingBox().stretch(0, 0, z)));
        }
    }
}
