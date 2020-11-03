package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.AIHelper;
import carpet.utils.extensions.AccessibleAITaskEntry;
import net.minecraft.entity.ai.EntityAITasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(EntityAITasks.class)
public class EntityAITasksMixin {
    @Redirect(method = "onUpdateTasks", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", remap = false), require = 3)
    private Object setReason(Iterator<?> iterator) {
        AccessibleAITaskEntry task = (AccessibleAITaskEntry) iterator.next();
        CarpetClientChunkLogger.setReason(() -> AIHelper.getInfo((EntityAITasks) (Object) this, task.getAction()));
        return task;
    }

    @Inject(method = "onUpdateTasks", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"), require = 2)
    private void resetReason(CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
