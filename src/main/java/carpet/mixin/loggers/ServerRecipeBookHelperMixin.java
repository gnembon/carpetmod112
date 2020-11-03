package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ServerRecipeBookHelper;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerRecipeBookHelper.class)
public class ServerRecipeBookHelperMixin {
    @Inject(method = "func_194327_a", at = @At("HEAD"))
    private void logRecipes(EntityPlayerMP p_194327_1_, IRecipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci) {
        // Added debugger for the instance people need help debuging why there recipes don't work. CARPET-XCOM
        if (LoggerRegistry.__recipes) {
            int i = 0;
            for (ResourceLocation r : CraftingManager.REGISTRY.getKeys()) {
                String index = Integer.toString(i);
                LoggerRegistry.getLogger("recipes").log(() -> new ITextComponent[]{
                        Messenger.s(null, index + ": " + r.getPath())
                });
                i++;
            }
        }
    }
}
