package carpet.mixin.summonNaturalLightning;

import carpet.CarpetSettings;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {
    @Redirect(method = "method_29272", at = @At(value = "NEW", target = "net/minecraft/entity/LightningEntity"))
    private LightningEntity summonNaturalLightning(World world, double x, double y, double z, boolean effectOnly, MinecraftServer server, CommandSource sender) {
        if (!CarpetSettings.summonNaturalLightning) return new LightningEntity(world, x, y,z, effectOnly);
        BlockPos bp = ((ServerWorldAccessor)world).invokeAdjustPosToNearbyEntity(new BlockPos(x, 0, z));
        if (!world.hasRain(bp)) return new LightningEntity(world, x, y,z, effectOnly);

        LocalDifficulty difficulty = world.getLocalDifficulty(bp);
        if (world.getGameRules().getBoolean("doMobSpawning") && world.random.nextDouble() < (double)difficulty.getLocalDifficulty() * 0.01D) {
            SkeletonHorseEntity horse = new SkeletonHorseEntity(world);
            horse.setTrapped(true);
            horse.setBreedingAge(0);
            horse.updatePosition(bp.getX(), bp.getY(), bp.getZ());
            world.spawnEntity(horse);

            WorldEditBridge.recordEntityCreation(sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null, world, horse);

            return new LightningEntity(world, bp.getX(), bp.getY(), bp.getZ(), true);
        } else {
            return new LightningEntity(world, bp.getX(), bp.getY(), bp.getZ(), false);
        }
    }
}
