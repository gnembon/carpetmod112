package carpet.helpers;

import carpet.carpetclient.CarpetClientServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class CustomCrafting {
    private static final String CARPET_DIRECTORY_RECIPES = "carpet/recipes";
    private static ArrayList<Pair<String, JsonObject>> recipeList = new ArrayList<>();
    private static HashSet<IRecipe> recipes = new HashSet<IRecipe>();

    public static boolean registerCustomRecipes(boolean result) throws IOException {
        if (!result) {
            return false;
        }

        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        File carpetDirectory = new File(CARPET_DIRECTORY_RECIPES);
        if (!carpetDirectory.exists()) {
            carpetDirectory.mkdirs();
        }

        Path path = Paths.get(CARPET_DIRECTORY_RECIPES);
        Iterator<Path> iterator = Files.walk(path).iterator();

        while (iterator.hasNext()) {
            Path path1 = iterator.next();

            if ("json".equals(FilenameUtils.getExtension(path1.toString()))) {
                Path path2 = path.relativize(path1);
                String s = FilenameUtils.removeExtension(path2.toString()).replaceAll("\\\\", "/");
                ResourceLocation resourcelocation = new ResourceLocation(s);
                BufferedReader bufferedreader = null;

                try {
                    boolean flag;

                    try {
                        bufferedreader = Files.newBufferedReader(path1);
                        JsonObject json = (JsonObject) JsonUtils.fromJson(gson, bufferedreader, JsonObject.class);
                        recipeList.add(Pair.of(s, json));
                        IRecipe ir = CraftingManager.parseRecipeJson(json);
                        recipes.add(ir);
                        CraftingManager.register(s, ir);
                    } catch (JsonParseException jsonparseexception) {
                        CraftingManager.LOGGER.error("Parsing error loading recipe " + resourcelocation, (Throwable) jsonparseexception);
                        flag = false;
                        return flag;
                    } catch (IOException ioexception) {
                        CraftingManager.LOGGER.error("Couldn't read recipe " + resourcelocation + " from " + path1, (Throwable) ioexception);
                        flag = false;
                        return flag;
                    }
                } finally {
                    IOUtils.closeQuietly((Reader) bufferedreader);
                }
            }
        }

        return true;
    }

    public static ArrayList<Pair<String, JsonObject>> getRecipeList() {
        return recipeList;
    }

    public static boolean filterCustomRecipesForOnlyCarpetClientUsers(IRecipe recipe, EntityPlayerMP player){
        return !recipes.contains(recipe) || CarpetClientServer.isPlayerRegistered(player);
    }
}

