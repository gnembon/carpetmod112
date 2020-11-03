package carpet.mixin.accessors;

import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NetHandlerPlayServer.class)
public interface NetHandlerPlayServerAccessor {
    @Invoker void invokeCaptureCurrentPosition();
}
