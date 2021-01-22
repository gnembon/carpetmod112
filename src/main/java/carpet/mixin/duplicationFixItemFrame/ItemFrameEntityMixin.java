package carpet.mixin.duplicationFixItemFrame;

import carpet.CarpetSettings;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.world.GameRuleManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity {
    public ItemFrameEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "dropHeldStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRuleManager;getBoolRule(Ljava/lang/String;)Z"))
    private boolean checkDead(GameRuleManager gameRules, String name) {
        return gameRules.getBoolRule(name) && (!CarpetSettings.duplicationFixItemFrame || !removed);
    }
}
