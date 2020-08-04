package carpet.mixin.accessors;

import net.minecraft.world.gen.structure.template.TemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TemplateManager.class)
public interface TemplateManagerAccessor {
    @Accessor String getBaseFolder();
}
