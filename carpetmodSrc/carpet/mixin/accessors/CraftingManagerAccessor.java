package carpet.mixin.accessors;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CraftingManager.class)
public interface CraftingManagerAccessor {
    @Invoker static IRecipe invokeParseRecipeJson(JsonObject json) { throw new AbstractMethodError(); }
}
