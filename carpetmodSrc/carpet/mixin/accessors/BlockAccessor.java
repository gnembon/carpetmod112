package carpet.mixin.accessors;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {
    @Invoker Block invokeSetLightOpacity(int opacity);
    @Invoker Block invokeSetTickRandomly(boolean shouldTick);
}
