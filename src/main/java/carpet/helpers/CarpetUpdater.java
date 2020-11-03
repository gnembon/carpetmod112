package carpet.helpers;

import carpet.CarpetSettings;
import carpet.utils.Messenger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class CarpetUpdater {
    static private String serverURL = "https://launcher.mojang.com/mc/game/1.12.2/server/886945bfb2b978778c3a0288fd7fab09d315b25f/server.jar";
    static private String githubURL = "https://api.github.com/repos/gnembon/carpetmod112/releases/latest";
    static private String vanillaJar = "update/MinecraftServer.1.12.2.jar";
    private static String carpetFileName = "update/Carpet.";
    private static final byte[] BUFFER = new byte[4096 * 1024];

    public static void updateCarpet(MinecraftServer server) {
        try {
            if (!checkFile("update/")) {
                new File("update/").mkdir();
            }
            String name = getCarpetFiles(server);

            if (name == null) {
                Messenger.print_server_message(server, "Already running latest version");
                return;
            }

            if (!checkFile(vanillaJar)) {
                Messenger.print_server_message(server, "Downloading vanilla Minecraft server jar");
                downloadUsingStream(serverURL, vanillaJar);
            }

            Messenger.print_server_message(server, "Building Carpet server jar");
            patch(name);
            Messenger.print_server_message(server, "Update complete (carpet server file found in /update/ folder)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER))!= -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }

    private static void patch(String path) throws Exception {
        ZipFile carpetZip = new ZipFile(path + ".zip");
        ZipFile originalJar = new ZipFile(vanillaJar);
        ZipOutputStream moddedJar = new ZipOutputStream(new FileOutputStream(path + ".jar"));

        Enumeration<? extends ZipEntry> entries = originalJar.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();

            String name = e.getName();
            if(carpetZip.getEntry(name) == null) {
                moddedJar.putNextEntry(e);
                if (!e.isDirectory()) {
                    copy(originalJar.getInputStream(e), moddedJar);
                }
                moddedJar.closeEntry();
            }
        }

        Enumeration<? extends ZipEntry> newentries = carpetZip.entries();
        while (newentries.hasMoreElements()) {
            ZipEntry e = newentries.nextElement();
            moddedJar.putNextEntry(e);
            if (!e.isDirectory()) {
                copy(carpetZip.getInputStream(e), moddedJar);
            }
            moddedJar.closeEntry();
        }

        originalJar.close();
        carpetZip.close();
        moddedJar.close();
    }

    private static boolean checkFile(String vanillaJar) {
        File file = new File(vanillaJar);
        return file.exists();
    }

    private static String getCarpetFiles(MinecraftServer server) throws Exception {
        String name = null;
        URL url = new URL(githubURL);
        URLConnection request = url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        String tag = rootobj.get("tag_name").getAsString();

        name = carpetFileName + tag;

        if (checkVersion(tag)) return null;
        if (checkFile(name + ".zip")) return name;

        JsonArray array = rootobj.get("assets").getAsJsonArray();
        JsonElement arrayElement = array.get(0);
        JsonObject assets = arrayElement.getAsJsonObject();
        String urlcarpet = assets.get("browser_download_url").getAsString();

        Messenger.print_server_message(server, "Downloading Carpet files");
        downloadUsingStream(urlcarpet, name + ".zip");

        return name;
    }

    private static boolean checkVersion(String tag) {
        return CarpetSettings.carpetVersion.equals(tag);
    }

    private static void downloadUsingStream(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }
}
