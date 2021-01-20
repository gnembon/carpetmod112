package carpet.carpetclient;

import carpet.CarpetSettings;
import net.minecraft.server.network.ServerPlayerEntity;

public class CarpetClientRuleTips {

    public static void getInfoRuleTip(ServerPlayerEntity sender, String rule) {
        String value = getRuleTip(rule);

        if (value != null) {
            sendRuleTip(sender, rule, value);
        }
    }

    public static void sendRuleTip(ServerPlayerEntity sender, String rule, String value) {
        CarpetClientRuleChanger.updatePlayerRuleInfo(sender, rule, value);
    }

    private static String getRuleTip(String rule) {
        return CarpetSettings.getDescription(rule);
    }
}
