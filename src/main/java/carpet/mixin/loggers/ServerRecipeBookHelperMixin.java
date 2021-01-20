package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.class_4522;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4522.class)
public class ServerRecipeBookHelperMixin {
    @Inject(method = "method_33169", at = @At("HEAD"))
    private void logRecipes(ServerPlayerEntity p_194327_1_, Recipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci) {
        // Added debugger for the instance people need help debuging why there recipes don't work. CARPET-XCOM
        if (LoggerRegistry.__recipes) {
            int i = 0;
            for (Identifier r : RecipeManager.recipes.getIds()) {
                String index = Integer.toString(i);
                LoggerRegistry.getLogger("recipes").log(() -> new Text[]{
                        Messenger.s(null, index + ": " + r.getPath())
                });
                i++;
            }
        }
    }
}
