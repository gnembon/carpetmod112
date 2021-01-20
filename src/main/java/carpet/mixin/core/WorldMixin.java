package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import carpet.utils.extensions.ExtendedWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Tickable;
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
    @Shadow @Final public Random random;

    private AtomicLong seed;

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Tickable;tick()V"))
    private void dontProcessTileEntities(Tickable tickable) {
        if (TickSpeed.process_entities) tickable.tick();
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26130(Lnet/minecraft/entity/Entity;)V"))
    private void dontProcessEntities(World world, Entity entity) {
        if (TickSpeed.process_entities) world.method_26130(entity);
    }

    @Override
    public long getRandSeed() {
        if (seed == null) {
            try {
                Field field = Random.class.getDeclaredField("seed");
                field.setAccessible(true);
                seed = (AtomicLong) field.get(random);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        return seed.get();
    }
}
