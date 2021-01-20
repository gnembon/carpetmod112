package carpet.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor boolean getCreateFire();
    @Accessor("field_23520") boolean getDamagesTerrain();
    @Accessor Random getRandom();
    @Accessor World getWorld();
    @Accessor("x") double getX();
    @Accessor("y") double getY();
    @Accessor("z") double getZ();
    @Accessor Entity getEntity();
    @Accessor float getPower();
    @Accessor List<BlockPos> getAffectedBlocks();
    @Accessor Map<PlayerEntity, Vec3d> getAffectedPlayers();
}
