package carpet.mixin.fillLimit;

import carpet.CarpetSettings;
import net.minecraft.command.CommandFill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CommandFill.class)
public class CommandFillMixin {
    @ModifyConstant(method = "execute", constant = @Constant(intValue = 32768))
    private int fillLimit(int orig) {
        return CarpetSettings.fillLimit;
    }
}
