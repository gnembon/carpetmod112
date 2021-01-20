package carpet.mixin.fillLimit;

import carpet.CarpetSettings;
import net.minecraft.server.command.FillCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FillCommand.class)
public class FillCommandMixin {
    @ModifyConstant(method = "method_29272", constant = @Constant(intValue = 32768))
    private int fillLimit(int orig) {
        return CarpetSettings.fillLimit;
    }
}
