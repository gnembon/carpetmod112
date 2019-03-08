package carpet.carpetclient;

import java.util.ArrayList;
import java.util.List;

import carpet.CarpetSettings;
import carpet.helpers.TickSpeed;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

public class CarpetClientMessageHandler {
    // Main packet data names
    public static final int GUI_ALL_DATA = 0;
    public static final int RULE_REQUEST = 1;
    public static final int VILLAGE_MARKERS = 2;
    public static final int BOUNDINGBOX_MARKERS = 3;
    public static final int TICKRATE_CHANGES = 4;
    public static final int LARGE_VILLAGE_MARKERS_START = 5;
    public static final int LARGE_VILLAGE_MARKERS = 6;
    public static final int LARGE_BOUNDINGBOX_MARKERS_START = 7;
    public static final int LARGE_BOUNDINGBOX_MARKERS = 8;
    public static final int CHUNK_LOGGER = 9;
	public static final int PISTON_UPDATES = 10;
    public static final int RANDOMTICK_DISPLAY = 11;

    public static void handler(EntityPlayerMP sender, PacketBuffer data) {
        int type = data.readInt();

        if (GUI_ALL_DATA == type) {
            sendAllGUIOptions();
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

    public static void sendAllGUIOptions() {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(GUI_ALL_DATA);

        String[] list = CarpetSettings.findAll(null);

        data.writeString(CarpetSettings.carpetVersion);
        data.writeFloat(TickSpeed.tickrate);
        data.writeInt(list.length);
        for (String rule : list) {
            String current = CarpetSettings.get(rule);
            String[] options = CarpetSettings.getOptions(rule);
            String def = CarpetSettings.getDefault(rule);
            boolean isfloat = CarpetSettings.isDouble(rule);

            data.writeString(rule);
            data.writeString(current);
            data.writeString(def);
            data.writeBoolean(isfloat);
            // data.writeInt(options.length);
            // for (String o : options) {
            // data.writeString(o);
            // }
        }

        CarpetClientServer.sender(data);
    }

    public static void sendNBTVillageData(EntityPlayerMP sender, NBTTagCompound compound) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.VILLAGE_MARKERS);

        data.writeCompoundTag(compound);

        if (!CarpetClientServer.sendProtected(data, sender)) {
            // Payload was too large, try large packets for newer CC versions
            NBTTagList villages = compound.getTagList("Villages", 10);

            data = new PacketBuffer(Unpooled.buffer());
            data.writeInt(CarpetClientMessageHandler.LARGE_VILLAGE_MARKERS_START);
            data.writeVarInt(villages.tagCount());
            CarpetClientServer.sender(data, sender);

            for (int i = 0; i < villages.tagCount(); i += 256) {
                data = new PacketBuffer(Unpooled.buffer());
                data.writeInt(CarpetClientMessageHandler.LARGE_VILLAGE_MARKERS);
                data.writeByte(Math.min(villages.tagCount() - i - 1, 255));
                for (int j = i; j < villages.tagCount() && j < i + 256; j++) {
                    data.writeCompoundTag(villages.getCompoundTagAt(j));
                }
                CarpetClientServer.sender(data, sender);
            }
        }
    }

    public static void sendNBTBoundingboxData(EntityPlayerMP sender, NBTTagCompound compound) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(CarpetClientMessageHandler.BOUNDINGBOX_MARKERS);

        data.writeCompoundTag(compound);

        if (!CarpetClientServer.sendProtected(data, sender)) {
            // Payload was too large, try large packets for newer CC versions
            NBTTagList boxes = compound.getTagList("Boxes", 9);
            compound.removeTag("Boxes");
            List<NBTTagCompound> allBoxes = new ArrayList<>();
            for (int i = 0; i < boxes.tagCount(); i++) {
                NBTTagList list = (NBTTagList) boxes.get(i);
                for (int j = 0; j < list.tagCount(); j++) {
                    allBoxes.add(list.getCompoundTagAt(j));
                }
            }

            data = new PacketBuffer(Unpooled.buffer());
            data.writeInt(CarpetClientMessageHandler.LARGE_BOUNDINGBOX_MARKERS_START);
            data.writeCompoundTag(compound);
            data.writeVarInt(allBoxes.size());
            CarpetClientServer.sender(data, sender);

            for (int i = 0; i < allBoxes.size(); i += 256) {
                data = new PacketBuffer(Unpooled.buffer());
                data.writeInt(CarpetClientMessageHandler.LARGE_BOUNDINGBOX_MARKERS);
                data.writeByte(Math.min(allBoxes.size() - i - 1, 255));
                for (int j = i; j < allBoxes.size() && j < i + 256; j++)
                    data.writeCompoundTag(allBoxes.get(j));
                CarpetClientServer.sender(data, sender);
            }
        }
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
        }catch(Exception e){ }
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
        }catch(Exception e){ }
        CarpetClientServer.sender(data, sender);
    }
}
