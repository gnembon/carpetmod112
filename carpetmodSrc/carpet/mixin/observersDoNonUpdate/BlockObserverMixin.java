package carpet.mixin.observersDoNonUpdate;

import carpet.CarpetSettings;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockObserver.class)
public class BlockObserverMixin {
    @Shadow @Final public static PropertyBool POWERED;

    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;withProperty(Lnet/minecraft/block/properties/IProperty;Ljava/lang/Comparable;)Lnet/minecraft/block/state/IBlockState;"))
    private <T extends Comparable<T>, V extends T> IBlockState noUpdateOnPlace(IBlockState state, IProperty<T> property, V value) {
        return state.withProperty(property, value).withProperty(POWERED, CarpetSettings.observersDoNonUpdate);
    }
}
