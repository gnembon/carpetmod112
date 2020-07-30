package carpet.mixin.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.server.CommandSaveAll;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSaveAll.class)
public abstract class CommandSaveAllMixin extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
