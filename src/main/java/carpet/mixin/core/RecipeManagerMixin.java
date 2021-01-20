package carpet.mixin.core;

import carpet.helpers.CustomCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.recipe.RecipeManager;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow private static boolean method_25727() { throw new AbstractMethodError(); }

    @Redirect(method = "method_25719", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;method_25727()Z"))
    private static boolean registerCustomRecipes() throws IOException {
        boolean result = method_25727();
        result = CustomCrafting.registerCustomRecipes(result);
        return result;
    }

    @Redirect(method = "method_25727", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;iterator()Ljava/util/Iterator;", remap = false))
    private static Iterator<Path> sortFiles(Stream<Path> stream) {
        TreeSet<Path> set = stream.collect(Collectors.toCollection(() -> new TreeSet<>((a, b) -> b.toString().compareTo(a.toString()))));
        return set.iterator();
    }
}
