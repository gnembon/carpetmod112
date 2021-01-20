package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.Messenger;
import net.minecraft.class_1999;
import net.minecraft.class_2010;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import java.util.List;

public abstract class CommandCarpetBase extends class_1999
{
    @Override
    public boolean method_29271(MinecraftServer server, class_2010 sender) {
        return true;
    }

    @Override
    public int method_28700() {
        return 0;
    }

    public void msg(class_2010 sender, List<Text> texts) { msg(sender, texts.toArray(new Text[0])); }
    public void msg(class_2010 sender, Text ... texts)
    {
        if (sender instanceof PlayerEntity)
        {
            for (Text t: texts) sender.sendMessage(t);
        }
        else
        {
            for (Text t: texts) method_28710(sender, this, t.method_32275());
        }
    }
    public boolean command_enabled(String command_name, class_2010 sender)
    {
        if (!CarpetSettings.get(command_name).equalsIgnoreCase("true"))
        {
            msg(sender, Messenger.m(null, "w Command is disabled in carpet settings"));
            if (!(sender instanceof PlayerEntity)) return false;
            if (CarpetSettings.locked)
            {
                Messenger.m((PlayerEntity)sender, "gi Ask your admin to enable it server config");
            }
            else
            {
                Messenger.m((PlayerEntity)sender,
                        "gi copy&pasta \"",
                        "gib /carpet "+command_name+" true", "/carpet "+command_name+" true",
                        "gi \"to enable it");
            }
            return false;
        }
        return true;
    }


}
