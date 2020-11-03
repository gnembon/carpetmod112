package carpet.mixin.accessors;

import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayerDigging.class)
public interface CPacketPlayerDiggingAccessor {
    @Accessor void setPosition(BlockPos pos);
    @Accessor void setFacing(EnumFacing facing);
    @Accessor void setAction(CPacketPlayerDigging.Action action);
}
