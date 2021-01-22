package carpet.mixin.observersDoNonUpdate;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {
    @Shadow @Final public static BooleanProperty POWERED;

    @Redirect(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Lnet/minecraft/block/BlockState;"))
    private <T extends Comparable<T>, V extends T> BlockState noUpdateOnPlace(BlockState state, Property<T> property, V value) {
        return state.with(property, value).with(POWERED, CarpetSettings.observersDoNonUpdate);
    }
}
