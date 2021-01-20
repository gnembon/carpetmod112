package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.AIHelper;
import carpet.utils.extensions.AccessibleGoalSelectorEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import net.minecraft.entity.ai.goal.GoalSelector;

@Mixin(GoalSelector.class)
public class GoalSelectorMixin {
    @Redirect(method = "method_34945", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", remap = false), require = 3)
    private Object setReason(Iterator<?> iterator) {
        AccessibleGoalSelectorEntry task = (AccessibleGoalSelectorEntry) iterator.next();
        CarpetClientChunkLogger.setReason(() -> AIHelper.getInfo((GoalSelector) (Object) this, task.getAction()));
        return task;
    }

    @Inject(method = "method_34945", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"), require = 2)
    private void resetReason(CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
