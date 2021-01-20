package carpet.mixin.accessors;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerActionC2SPacket.class)
public interface PlayerActionC2SPacketAccessor {
    @Accessor void setPos(BlockPos pos);
    @Accessor void setDirection(Direction facing);
    @Accessor void setAction(PlayerActionC2SPacket.Action action);
}
