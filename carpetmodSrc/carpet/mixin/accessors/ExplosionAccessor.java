package carpet.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor boolean getCausesFire();
    @Accessor boolean getDamagesTerrain();
    @Accessor Random getRandom();
    @Accessor World getWorld();
    @Accessor("x") double getX();
    @Accessor("y") double getY();
    @Accessor("z") double getZ();
    @Accessor Entity getExploder();
    @Accessor float getSize();
    @Accessor List<BlockPos> getAffectedBlockPositions();
    @Accessor Map<EntityPlayer, Vec3d> getPlayerKnockbackMap();
}
