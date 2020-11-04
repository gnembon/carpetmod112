package carpet.mixin.summonNaturalLightning;

import carpet.CarpetSettings;
import carpet.mixin.accessors.WorldServerAccessor;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandSummon;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSummon.class)
public class CommandSummonMixin {
    @Redirect(method = "execute", at = @At(value = "NEW", target = "net/minecraft/entity/effect/EntityLightningBolt"))
    private EntityLightningBolt summonNaturalLightning(World world, double x, double y, double z, boolean effectOnly, MinecraftServer server, ICommandSender sender) {
        if (!CarpetSettings.summonNaturalLightning) return new EntityLightningBolt(world, x, y,z, effectOnly);
        BlockPos bp = ((WorldServerAccessor)world).invokeAdjustPosToNearbyEntity(new BlockPos(x, 0, z));
        if (!world.isRainingAt(bp)) return new EntityLightningBolt(world, x, y,z, effectOnly);

        DifficultyInstance difficulty = world.getDifficultyForLocation(bp);
        if (world.getGameRules().getBoolean("doMobSpawning") && world.rand.nextDouble() < (double)difficulty.getAdditionalDifficulty() * 0.01D) {
            EntitySkeletonHorse horse = new EntitySkeletonHorse(world);
            horse.setTrap(true);
            horse.setGrowingAge(0);
            horse.setPosition(bp.getX(), bp.getY(), bp.getZ());
            world.spawnEntity(horse);

            WorldEditBridge.recordEntityCreation(sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null, world, horse);

            return new EntityLightningBolt(world, bp.getX(), bp.getY(), bp.getZ(), true);
        } else {
            return new EntityLightningBolt(world, bp.getX(), bp.getY(), bp.getZ(), false);
        }
    }
}
