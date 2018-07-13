package carpet.carpetclient;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import carpet.CarpetSettings;
import carpet.CarpetSettings.CarpetSettingEntry;
import carpet.utils.Messenger;
import net.minecraft.network.PacketBuffer;

public class CarpetClientRuleChanger {

	// Minor rule changing packet names
	private static final int CHANGE_RULE = 0;
	private static final int CHANGE_TEXT_RULE = 1;
	private static final int RESET_RULE = 2;
	private static final int REQUEST_RULE_TIP = 3;

	static void ruleChanger(EntityPlayerMP sender, PacketBuffer data) {
		int type = data.readInt();
		String rule = data.readStringFromBuffer(100);

		if (CHANGE_RULE == type) {
			if (sender.canCommandSenderUseCommand(2, "carpet")) {
				CarpetSettingEntry entry = CarpetSettings.getCarpetSetting(rule);
				String value = entry.getNextValue();
				ruleChangeLogic(sender, rule, value);
			} else {
				Messenger.m(sender, "r You do not have permissions to change the rules.");
			}
		} else if (CHANGE_TEXT_RULE == type) {
			if (sender.canCommandSenderUseCommand(2, "carpet")) {
				String value = data.readStringFromBuffer(100);
				ruleChangeLogic(sender, rule, value);
			} else {
				Messenger.m(sender, "r You do not have permissions to change the rules.");
			}
		} else if (RESET_RULE == type) {
			if (sender.canCommandSenderUseCommand(2, "carpet")) {
				CarpetSettingEntry entry = CarpetSettings.getCarpetSetting(rule);
				String value = entry.getDefault();
				ruleChangeLogic(sender, rule, value);
			} else {
				Messenger.m(sender, "r You do not have permissions to change the rules.");
			}
		} else if (REQUEST_RULE_TIP == type) {
			CarpetClientRuleTips.getInfoRuleTip(sender, rule);
		}
	}

	private static void ruleChangeLogic(EntityPlayerMP sender, String rule, String value) {
		CarpetSettings.set(rule, value);
		String s = CarpetSettings.get(rule).getToast() + " is set to: " + CarpetSettings.getString(rule);
        Messenger.print_server_message(sender.getEntityWorld().getMinecraftServer(), s);
		updateCarpetClientsRule(rule, value);
	}

	public static void updateCarpetClientsRule(String rule, String value) {
		PacketBuffer data = new PacketBuffer(Unpooled.buffer());

		data.writeInt(CarpetClientMessageHandler.RULE_REQUEST);
		data.writeString(rule);
		data.writeInt(CHANGE_RULE);
		data.writeString(value);

		CarpetClientServer.sender(data);
	}

	static void updatePlayerRuleInfo(EntityPlayerMP sender, String rule, String value) {
		PacketBuffer data = new PacketBuffer(Unpooled.buffer());

		data.writeInt(CarpetClientMessageHandler.RULE_REQUEST);
		data.writeString(rule);
		data.writeInt(REQUEST_RULE_TIP);
		data.writeString(value);

		CarpetClientServer.sender(data, sender);
	}
}
