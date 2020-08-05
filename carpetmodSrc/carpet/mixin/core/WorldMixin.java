package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import net.minecraft.entity.Entity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    private void dontProcessTileEntities(ITickable tickable) {
        if (TickSpeed.process_entities) tickable.update();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"))
    private void dontProcessEntities(World world, Entity entity) {
        if (TickSpeed.process_entities) world.updateEntity(entity);
    }
}
