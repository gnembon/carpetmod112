package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

public class KillLogHelper {

    public static void onSweep(EntityPlayer player, int count)
    {
        LoggerRegistry.getLogger("kills").log( () -> new ITextComponent[]{
                Messenger.m(null, "g "+player.getGameProfile().getName()+" smacked ","r "+count,"g  entities with sweeping")},
                "ATTACKER", player.getCachedUniqueIdString(),
                "COUNT", count);
    }

    public static void onNonSweepAttack(EntityPlayer player)
    {
        LoggerRegistry.getLogger("kills").log( () -> new ITextComponent[]{
                Messenger.m(null, "g "+player.getGameProfile().getName()+" smacked ","r 1","g  (no sweeping)")},
                "ATTACKER", player.getCachedUniqueIdString(),
                "COUNT", 1);
    }

    public static void onDudHit(EntityPlayer player)
    {
        LoggerRegistry.getLogger("kills").log( () -> new ITextComponent[]{
                Messenger.m(null, "g "+player.getGameProfile().getName()+" dud hot = no one affected")},
                "ATTACKER", player.getCachedUniqueIdString(),
                "COUNT", 0);
    }
}
