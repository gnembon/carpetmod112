package carpet.mixin.core;

import carpet.helpers.CustomCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {
    private static final ThreadLocal<ServerPlayerEntity> tlPlayer = new ThreadLocal<>();

    @Redirect(method = {
        "method_33855",
        "method_33858"
    }, at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean filter(List<Recipe> list, Object e, List<Recipe> recipesIn, ServerPlayerEntity player) {
        Recipe recipe = (Recipe) e;
        if (!CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        return list.add(recipe);
    }

    @Redirect(method = "method_33860", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean filter(List<Recipe> list, Object e) {
        Recipe recipe = (Recipe) e;
        ServerPlayerEntity player = tlPlayer.get();
        if (player != null && !CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        if (recipe == null) System.out.println("found null recipe");
        return list.add(recipe);
    }

    @Inject(method = "sendInitRecipesPacket", at = @At("HEAD"))
    private void onInitStart(ServerPlayerEntity player, CallbackInfo ci) {
        tlPlayer.set(player);
    }

    @Inject(method = "sendInitRecipesPacket", at = @At("RETURN"))
    private void onInitEnd(ServerPlayerEntity player, CallbackInfo ci) {
        tlPlayer.set(null);
    }
}
