package carpet.mixin.ai;

import carpet.helpers.AIHelper;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GoalSelector.class)
public class GoalSelectorMixin {
    @Inject(method = "method_34945", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z", remap = false, shift = At.Shift.AFTER))
    private void update1(CallbackInfo ci) {
        AIHelper.update((GoalSelector) (Object) this);
    }

    @Inject(method = "method_34945", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false, shift = At.Shift.AFTER))
    private void update2(CallbackInfo ci) {
        AIHelper.update((GoalSelector) (Object) this);
    }

    @Inject(method = "method_34945", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", remap = false, shift = At.Shift.AFTER))
    private void update3(CallbackInfo ci) {
        AIHelper.update((GoalSelector) (Object) this);
    }
}
