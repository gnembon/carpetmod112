package carpet.mixin.duplicationFixItemFrame;

import carpet.CarpetSettings;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity {
    public ItemFrameEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "method_24656", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;method_25916(Ljava/lang/String;)Z"))
    private boolean checkDead(GameRules gameRules, String name) {
        return gameRules.method_25916(name) && (!CarpetSettings.duplicationFixItemFrame || !removed);
    }
}
