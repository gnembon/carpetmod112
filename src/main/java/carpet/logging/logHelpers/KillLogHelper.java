package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class KillLogHelper {

    public static void onSweep(PlayerEntity player, int count)
    {
        LoggerRegistry.getLogger("kills").log( () -> new Text[]{
                Messenger.m(null, "g "+player.getGameProfile().getName()+" smacked ","r "+count,"g  entities with sweeping")},
                "ATTACKER", player.getUuidAsString(),
                "COUNT", count);
    }

    public static void onNonSweepAttack(PlayerEntity player)
    {
        LoggerRegistry.getLogger("kills").log( () -> new Text[]{
                Messenger.m(null, "g "+player.getGameProfile().getName()+" smacked ","r 1","g  (no sweeping)")},
                "ATTACKER", player.getUuidAsString(),
                "COUNT", 1);
    }

    public static void onDudHit(PlayerEntity player)
    {
        LoggerRegistry.getLogger("kills").log( () -> new Text[]{
                Messenger.m(null, "g "+player.getGameProfile().getName()+" dud hot = no one affected")},
                "ATTACKER", player.getUuidAsString(),
                "COUNT", 0);
    }
}
