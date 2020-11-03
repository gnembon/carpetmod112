package carpet.mixin.core;

import carpet.helpers.CustomCrafting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBookServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeBookServer.class)
public class RecipeBookServerMixin {
    private static final ThreadLocal<EntityPlayerMP> tlPlayer = new ThreadLocal<>();

    @Redirect(method = {
        "add",
        "remove"
    }, at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean filter(List<IRecipe> list, Object e, List<IRecipe> recipesIn, EntityPlayerMP player) {
        IRecipe recipe = (IRecipe) e;
        if (!CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        return list.add(recipe);
    }

    @Redirect(method = "getRecipes", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean filter(List<IRecipe> list, Object e) {
        IRecipe recipe = (IRecipe) e;
        EntityPlayerMP player = tlPlayer.get();
        if (player != null && !CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        if (recipe == null) System.out.println("found null recipe");
        return list.add(recipe);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitStart(EntityPlayerMP player, CallbackInfo ci) {
        tlPlayer.set(player);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitEnd(EntityPlayerMP player, CallbackInfo ci) {
        tlPlayer.set(null);
    }
}
