package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import carpet.utils.extensions.ExtendedWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(World.class)
public class WorldMixin implements ExtendedWorld {
    @Shadow @Final public Random rand;

    private AtomicLong seed;

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    private void dontProcessTileEntities(ITickable tickable) {
        if (TickSpeed.process_entities) tickable.update();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"))
    private void dontProcessEntities(World world, Entity entity) {
        if (TickSpeed.process_entities) world.updateEntity(entity);
    }

    @Override
    public long getRandSeed() {
        if (seed == null) {
            try {
                Field field = Random.class.getDeclaredField("seed");
                field.setAccessible(true);
                seed = (AtomicLong) field.get(rand);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        return seed.get();
    }
}
