package carpet.carpetclient;

import java.util.ArrayList;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import carpet.CarpetSettings;
import carpet.CarpetSettings.CarpetSettingEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import carpet.helpers.TickSpeed;

public class CarpetClientMessageHandler {
	// Main packet data names
	public static final int GUI_ALL_DATA = 0;
	public static final int RULE_REQUEST = 1;
	public static final int VILLAGE_MARKERS = 2;
	public static final int BOUNDINGBOX_MARKERS = 3;
	public static final int TICKRATE_CHANGES = 4;

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

		ArrayList<CarpetSettingEntry> list = CarpetSettings.getAllCarpetSettings();

		data.writeString(CarpetSettings.carpetVersion);
		data.writeFloat(TickSpeed.tickrate);
		data.writeInt(list.size());
		for (CarpetSettingEntry cse : list) {
			String rule = cse.getName();
			String current = cse.getStringValue();
			String[] options = cse.getOptions();
			String def = cse.getDefault();
			boolean isfloat = cse.getIsFloat();

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
}
