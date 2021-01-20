package carpet.mixin.disableBookBan;

import carpet.CarpetSettings;
import net.minecraft.network.SizePrepender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SizePrepender.class)
public class SizePrependerMixin {
    @ModifyConstant(method = "encode", constant = @Constant(intValue = 3, ordinal = 0))
    private int disableBookBan(int three) {
        // Disable this check to prevent book banning serverside. CARPET-XCOM
        return CarpetSettings.disableBookBan ? 5 : three;
    }
}
