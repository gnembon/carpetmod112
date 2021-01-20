package carpet.mixin.accessors;

import com.google.gson.JsonObject;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Invoker("method_25723") static Recipe invokeParseRecipeJson(JsonObject json) { throw new AbstractMethodError(); }
}
