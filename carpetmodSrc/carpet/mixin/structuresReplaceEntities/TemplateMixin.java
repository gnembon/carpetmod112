package carpet.mixin.structuresReplaceEntities;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Template.class)
public abstract class TemplateMixin {
    @Shadow private BlockPos size;

    @Shadow protected static Vec3d transformedVec3d(Vec3d vec, Mirror mirrorIn, Rotation rotationIn) { throw new AbstractMethodError(); }

    @Inject(method = "addEntitiesToWorld", at = @At("HEAD"))
    private void replaceEntities(World worldIn, BlockPos pos, Mirror mirrorIn, Rotation rotationIn, StructureBoundingBox aabb, CallbackInfo ci) {
        if (!CarpetSettings.structuresReplaceEntities) return;
        AxisAlignedBB box = new AxisAlignedBB(pos, new BlockPos(transformedVec3d((new Vec3d(pos.add(size))), mirrorIn, rotationIn)));
        for (Entity e : worldIn.getEntitiesWithinAABB(Entity.class, box, entity -> !(entity instanceof EntityPlayer))) {
            System.out.println(e);
            e.onKillCommand();
        }
    }
}
