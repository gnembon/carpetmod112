package carpet.mixin.core;

import net.minecraft.class_1999;
import net.minecraft.server.dedicated.command.SaveAllCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SaveAllCommand.class)
public abstract class SaveAllCommandMixin extends class_1999 {
    @Override
    public int method_28700() {
        return 2;
    }
}
