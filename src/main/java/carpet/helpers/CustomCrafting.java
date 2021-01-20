package carpet.helpers;

import carpet.carpetclient.CarpetClientServer;
import carpet.mixin.accessors.RecipeManagerAccessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CustomCrafting {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CARPET_DIRECTORY_RECIPES = "carpet/recipes";
    private static ArrayList<Pair<String, JsonObject>> recipeList = new ArrayList<>();
    private static HashSet<Recipe> recipes = new HashSet<Recipe>();

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
                Identifier resourcelocation = new Identifier(s);
                BufferedReader bufferedreader = null;

                try {
                    try {
                        bufferedreader = Files.newBufferedReader(path1);
                        JsonObject json = JsonHelper.deserialize(gson, bufferedreader, JsonObject.class);
                        recipeList.add(Pair.of(s, json));
                        Recipe ir = RecipeManagerAccessor.invokeParseRecipeJson(json);
                        recipes.add(ir);
                        RecipeManager.register(s, ir);
                    } catch (JsonParseException jsonparseexception) {
                        LOGGER.error("Parsing error loading recipe " + resourcelocation, jsonparseexception);
                        return false;
                    } catch (IOException ioexception) {
                        LOGGER.error("Couldn't read recipe " + resourcelocation + " from " + path1, ioexception);
                        return false;
                    }
                } finally {
                    IOUtils.closeQuietly(bufferedreader);
                }
            }
        }

        return true;
    }

    public static ArrayList<Pair<String, JsonObject>> getRecipeList() {
        return recipeList;
    }

    public static boolean filterCustomRecipesForOnlyCarpetClientUsers(Recipe recipe, ServerPlayerEntity player){
        return !recipes.contains(recipe) || CarpetClientServer.isPlayerRegistered(player);
    }
}

