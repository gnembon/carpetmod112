package carpet.carpetclient;

import java.util.ArrayList;
import java.util.List;

import carpet.CarpetSettings;
import carpet.helpers.CustomCrafting;
import carpet.helpers.TickSpeed;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.tuple.Pair;

public class CarpetClientMessageHandler {
    // Main packet data names
    public static final int GUI_ALL_DATA = 0;
    public static final int RULE_REQUEST = 1;
    public static final int VILLAGE_MARKERS = 2;
    public static final int BOUNDINGBOX_MARKERS = 3;
    public static final int TICKRATE_CHANGES = 4;
    public static final int CHUNK_LOGGER = 5;
    public static final int PISTON_UPDATES = 6;
    public static final int RANDOMTICK_DISPLAY = 7;
    public static final int CUSTOM_RECIPES = 8;

    private static final int NET_VERSION = 1;

    public static void handler(EntityPlayerMP sender, PacketBuffer data) {
        int type = data.readInt();

        if (GUI_ALL_DATA == type) {
            sendAllGUIOptions(sender);
        } else if (RULE_REQUEST == type) {
            ruleRequest(sender, data);
        } else if (VILLAGE_MARKERS == type) {
            registerVillagerMarkers(sender, data);
        } else if (BOUNDINGBOX_MARKERS == type) {
            boundingboxRequest(sender, data);
        } else if (CHUNK_LOGGER == type) {
            CarpetClientChunkLogger.logger.registerPlayer(sender, data);
        } else if (RANDOMTICK_DISPLAY == type) {
            CarpetClientRandomtickingIndexing.register(sender, data);
        } else if (CUSTOM_RECIPES == type) {
            confirmationReceivedCustomRecipesSendUpdate(sender);
        }
    }

    private static void registerVillagerMarkers(EntityPlayerMP sender, PacketBuffer data) {
        CarpetClientMarkers.registerVillagerMarkers(sender, data);
    }

    private static void boundingboxRequest(EntityPlayerMP sender, PacketBuffer data) {
        CarpetClientMarkers.updateClientBoundingBoxMarkers(sender, data);
    }

    private static void ruleRequest(EntityPlayerMP sender, PacketBuffer data) {
        CarpetClientRuleChanger.ruleChanger(sender, data);
    }

    public static void sendAllGUIOptions(EntityPlayerMP sender) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(GUI_ALL_DATA);

        String[] list = CarpetSettings.findAll(null);

        NBTTagCompound chunkData = new NBTTagCompound();

        chunkData.setString("carpetVersion", CarpetSettings.carpetVersion);
        chunkData.setFloat("tickrate", TickSpeed.tickrate);
        chunkData.setInteger("netVersion", NET_VERSION);
        NBTTagList listNBT = new NBTTagList();
        for (String rule : list) {
            String current = CarpetSettings.get(rule);
            String[] options = CarpetSettings.getOptions(rule);
            String def = CarpetSettings.getDefault(rule);
            boolean isfloat = CarpetSettings.isDouble(rule);

            NBTTagCompound ruleNBT = new NBTTagCompound();
            ruleNBT.setString("rule", rule);
            ruleNBT.setString("current", current);
            ruleNBT.setString("default", def);
            ruleNBT.setBoolean("isfloat", isfloat);
            listNBT.appendTag(ruleNBT);
        }
        chunkData.setTag("ruleList", listNBT);

        try {
            data.writeCompoundTag(chunkData);
        } catch (Exception e) {
        }

        CarpetClientServer.sender(data, sender);
    }

    public static void sendNBTVillageData(EntityPlayerMP sender, NBTTagCompound compound) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.VILLAGE_MARKERS);

        data.writeCompoundTag(compound);

        CarpetClientServer.sender(data, sender);
    }

    public static void sendNBTBoundingboxData(EntityPlayerMP sender, NBTTagCompound compound) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.BOUNDINGBOX_MARKERS);

        data.writeCompoundTag(compound);

        CarpetClientServer.sender(data, sender);
    }

    public static void sendTickRateChanges() {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.TICKRATE_CHANGES);
        data.writeFloat(TickSpeed.tickrate);

        CarpetClientServer.sender(data);
    }

    public static void sendNBTChunkData(EntityPlayerMP sender, int dataType, NBTTagCompound compound) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.CHUNK_LOGGER);
        data.writeInt(dataType);
        try {
            data.writeCompoundTag(compound);
        } catch (Exception e) {
        }
        CarpetClientServer.sender(data, sender);
    }

    public static void sendPistonUpdate() {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.PISTON_UPDATES);

        CarpetClientServer.sender(data);
    }

    public static void sendNBTRandomTickData(EntityPlayerMP sender, NBTTagCompound compound) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.RANDOMTICK_DISPLAY);
        try {
            data.writeCompoundTag(compound);
        } catch (Exception e) {
        }
        CarpetClientServer.sender(data, sender);
    }

    public static void sendCustomRecipes(EntityPlayerMP sender) {
        if (CustomCrafting.getRecipeList().size() == 0) return;
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CUSTOM_RECIPES);

        NBTTagCompound chunkData = new NBTTagCompound();

        NBTTagList listNBT = new NBTTagList();
        for (Pair<String, JsonObject> pair : CustomCrafting.getRecipeList()) {
            NBTTagCompound recipe = new NBTTagCompound();
            recipe.setString("name", pair.getKey());
            recipe.setString("recipe", pair.getValue().toString());
            listNBT.appendTag(recipe);
        }
        chunkData.setTag("recipeList", listNBT);

        try {
            data.writeCompoundTag(chunkData);
        } catch (Exception e) {
        }

        CarpetClientServer.sender(data, sender);
    }

    public static void confirmationReceivedCustomRecipesSendUpdate(EntityPlayerMP sender) {
        sender.getRecipeBook().init(sender);
    }
}
