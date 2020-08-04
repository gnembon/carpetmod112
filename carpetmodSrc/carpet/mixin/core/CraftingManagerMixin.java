package carpet.mixin.core;

import carpet.helpers.CustomCrafting;
import net.minecraft.item.crafting.CraftingManager;
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

@Mixin(CraftingManager.class)
public abstract class CraftingManagerMixin {
    @Shadow private static boolean parseJsonRecipes() { throw new AbstractMethodError(); }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;parseJsonRecipes()Z"))
    private static boolean registerCustomRecipes() throws IOException {
        boolean result = parseJsonRecipes();
        result = CustomCrafting.registerCustomRecipes(result);
        return result;
    }

    @Redirect(method = "parseJsonRecipes", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;iterator()Ljava/util/Iterator;", remap = false))
    private static Iterator<Path> sortFiles(Stream<Path> stream) {
        TreeSet<Path> set = stream.collect(Collectors.toCollection(() -> new TreeSet<>((a, b) -> b.toString().compareTo(a.toString()))));
        return set.iterator();
    }
}
