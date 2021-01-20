package carpet.mixin.combinePotionDuration;

import carpet.CarpetSettings;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityStatusEffectS2CPacket.class)
public class EntityStatusEffectS2CPacketMixin {
    @ModifyConstant(method = "<init>(ILnet/minecraft/entity/effect/StatusEffectInstance;)V", constant = @Constant(intValue = 32767, ordinal = 0))
    private int getDurationCap(int cap) {
        // Fix the duration cap for sending smaller packets for effect durations CARPET-XCOM
        return CarpetSettings.combinePotionDuration == 0 ? cap : Integer.MAX_VALUE;
    }
}
