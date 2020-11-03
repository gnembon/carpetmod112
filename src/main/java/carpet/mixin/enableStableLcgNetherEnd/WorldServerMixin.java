package carpet.mixin.enableStableLcgNetherEnd;

import carpet.CarpetSettings;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Unique private boolean shouldLcg() {
        return !CarpetSettings.enableStableLCGNetherEnd || provider.getDimensionType().getId() == 0;
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", remap = false))
    private int nextInt(Random random, int bound) {
        return shouldLcg() ? random.nextInt(bound) : -1;
    }
}
