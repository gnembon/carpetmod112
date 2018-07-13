package carpet.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nullable;

import carpet.utils.Messenger;
import net.minecraft.command.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import carpet.CarpetSettings;
import carpet.CarpetSettings.CarpetSettingEntry;
import net.minecraft.util.text.ITextComponent;

public class CommandCarpet extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */
    public String getCommandUsage(ICommandSender sender)
    {
        return "carpet <rule> <value>";
    }
    public String getCommandName()
    {
        return "carpet";
    }

    /**
     * Return the required permission level for this command.
     */
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    private ITextComponent displayInteractiveSetting(CarpetSettingEntry e)
    {
        List<Object> args = new ArrayList<>();
        args.add("w - "+e.getName()+" ");
        args.add("!/carpet "+e.getName());
        args.add("^y "+e.getToast());
        for (String option: e.getOptions())
        {
            String style = e.isDefault()?"g":(option.equalsIgnoreCase(e.getDefault())?"y":"e");
            if (option.equalsIgnoreCase(e.getDefault()))
                style = style+"b";
            else if (option.equalsIgnoreCase(e.getStringValue()))
                style = style+"u";
            args.add(style+" ["+option+"]");
            if (!option.equalsIgnoreCase(e.getStringValue()))
            {
                args.add("!/carpet " + e.getName() + " " + option);
                args.add("^w switch to " + option);
            }
            args.add("w  ");
        }
        args.remove(args.size()-1);
        return Messenger.m(null, args.toArray(new Object[0]));
    }

    public void list_settings(ICommandSender sender, String title, CarpetSettingEntry[] settings_list)
    {
        if (sender instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) sender;
            Messenger.m(player,String.format("wb %s:",title));
            Arrays.stream(settings_list).forEach(e -> Messenger.m(player,displayInteractiveSetting(e)));
        }
        else
        {
            notifyCommandListener(sender, this, String.format("%s:",title));
            Arrays.stream(settings_list).forEach(e -> notifyCommandListener(sender, this, String.format(" - %s", e.toString())));
        }
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (CarpetSettings.locked)
        {
            list_settings(sender, "Carpet Mod admin locked with the following settings",CarpetSettings.find_nondefault(server));
            return;
        }
        String tag = null;
        try
        {
            if (args.length == 0)
            {
                list_settings(sender, "Current CarpetMod Settings", CarpetSettings.find_nondefault(server));
                notifyCommandListener(sender, this, "Carpet Mod version: "+CarpetSettings.carpetVersion);
                if (sender instanceof EntityPlayer)
                {
                    List<Object> tags = new ArrayList<>();
                    tags.add("w Browse Categories:\n");
                    for (String t : CarpetSettings.default_tags)
                    {
                        tags.add("c [" + t+"]");
                        tags.add("^g list all " + t + " settings");
                        tags.add("!/carpet list " + t);
                        tags.add("w  ");
                    }
                    tags.remove(tags.size() - 1);
                    Messenger.m((EntityPlayer)sender, tags.toArray(new Object[0]));
                }
                return;
            }
            if (args.length == 1 && "list".equalsIgnoreCase(args[0]))
            {
                list_settings(sender, "All CarpetMod Settings", CarpetSettings.find_all(null));
                notifyCommandListener(sender, this, "Carpet Mod version: "+CarpetSettings.carpetVersion);
                return;
            }
            if ("defaults".equalsIgnoreCase(args[0])) // empty tag
            {
                list_settings(sender, "Current CarpetMod Startup Settings from carpet.conf", CarpetSettings.find_startup_overrides(server));
                notifyCommandListener(sender, this, "Carpet Mod version: "+CarpetSettings.carpetVersion);
                return;
            }
            if ("use".equalsIgnoreCase(args[0])) // empty tag
            {
                if ("default".equalsIgnoreCase(args[1]))
                {
                    CarpetSettings.resetToUserDefaults(server);
                    return;
                }
                if ("vanilla".equalsIgnoreCase(args[1]))
                {
                    CarpetSettings.resetToVanilla();
                    return;
                }
                if ("survival".equalsIgnoreCase(args[1]))
                {
                    CarpetSettings.resetToSurvival();
                    return;
                }
                if ("creative".equalsIgnoreCase(args[1]))
                {
                    CarpetSettings.resetToCreative();
                    return;
                }
                if ("bugfixes".equalsIgnoreCase(args[1]))
                {
                    CarpetSettings.resetToBugFixes();
                    return;
                }
                throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
            }

            if (args.length >= 2 && "list".equalsIgnoreCase(args[0]))
            {
                tag = args[1].toLowerCase();
                args = Arrays.copyOfRange(args, 2, args.length);
                //no return; continue
            }
            if (args.length == 0)
            {
                list_settings(sender, String.format("CarpetMod Settings matching \"%s\"", tag),CarpetSettings.find_all(tag)) ;
                return;
            }
            if ("setDefault".equalsIgnoreCase(args[0]))
            {
                if (args.length != 3)
                {
                    throw new WrongUsageException("/carpet setDefault <setting> <value>");
                }
                boolean success = CarpetSettings.add_or_set_permarule(server, args[1], args[2]);
                if (success)
                {
                    notifyCommandListener(sender, this,
                        CarpetSettings.get(args[1]).getName() +" will default to: "+args[2]);
                }
                else
                {
                    throw new WrongUsageException("Unknown setting");
                }
                return;
            }
            if ("removeDefault".equalsIgnoreCase(args[0]))
            {
                if (args.length != 2)
                {
                    throw new WrongUsageException("/carpet removeDefault <setting>");
                }
                boolean success = CarpetSettings.remove_permrule(server, args[1]);
                if (success)
                {
                    notifyCommandListener(sender, this,
                        CarpetSettings.get(args[1]).getName() +" will not be set upon restart");
                }
                else
                {
                    throw new WrongUsageException("Unknown setting");
                }
                return;
            }
            if (CarpetSettings.get(args[0]) == CarpetSettings.FalseEntry)
            {
                throw new WrongUsageException("Unknown setting: "+args[0]);
            }
            if (args.length == 2)
            {
                boolean success = CarpetSettings.set(args[0], args[1]);
                if (!success)
                {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                CarpetSettingEntry en = CarpetSettings.get(args[0]);
                msg(sender, Messenger.m(null, "w "+en.toString()+", ", "c [change permanently?]",
                        "^w Click to keep the settings in carpet.conf to save across restarts",
                        "?/carpet setDefault "+en.getName()+" "+en.getStringValue()));
                return;
            }
            if (sender instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer)sender;
                CarpetSettingEntry entry = CarpetSettings.get(args[0]);
                Messenger.s(player, "");
                Messenger.m(player, "wb "+entry.getName(),"!/carpet "+entry.getName(),"^g refresh");
                Messenger.s(player, entry.getToast());

                Arrays.stream(entry.getInfo()).forEach(s -> Messenger.s(player, " "+s,"g"));

                List<ITextComponent> tags = new ArrayList<>();
                tags.add(Messenger.m(null, "w Tags: "));
                for (String t: entry.getTags())
                {
                    tags.add(Messenger.m(null, "c ["+t+"]", "^g list all "+t+" settings","!/carpet list "+t));
                    tags.add(Messenger.s(null, ", "));
                }
                tags.remove(tags.size()-1);
                Messenger.m(player, tags.toArray(new Object[0]));
//
                Messenger.m(player, "w Current value: ",String.format("%s %s (%s value)",entry.getBoolValue()?"lb":"nb", entry.getStringValue(),entry.isDefault()?"default":"modified"));
                List<ITextComponent> options = new ArrayList<>();
                options.add(Messenger.m(null, "w Options: ", "y [ "));
                for (String o: entry.getOptions())
                {
                    options.add(Messenger.m(null,
                            String.format("%s%s %s",(o.equals(entry.getDefault()))?"u":"", (o.equals(entry.getStringValue()))?"bl":"y", o ),
                            "^g switch to "+o,
                            String.format("?/carpet %s %s",entry.getName(),o)));
                    options.add(Messenger.s(null, " "));
                }
                options.remove(options.size()-1);
                options.add(Messenger.m(null, "y  ]"));
                Messenger.m(player, options.toArray(new Object[0]));
            }
            else
            {
                notifyCommandListener(sender, this,
                        CarpetSettings.get(args[0]).getName() +" is set to: "+CarpetSettings.getString(args[0]));
            }
            // Updates the carpet client with the changed rule.
        }
        catch(CommandException e)
        {
            if (e instanceof WrongUsageException)
            {
                throw e;
            }
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (CarpetSettings.locked)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 2 && "list".equalsIgnoreCase(args[0]))
        {
            return getListOfStringsMatchingLastWord(args, CarpetSettings.default_tags);
        }
        if (args.length == 2 && "list".equalsIgnoreCase(args[0]))
        {
            return getListOfStringsMatchingLastWord(args, CarpetSettings.default_tags);
        }
        if (args.length == 2 && "use".equalsIgnoreCase(args[0]))
        {
            return getListOfStringsMatchingLastWord(args, "creative", "survival", "default","vanilla", "bugfixes");
        }
        String tag = null;
        if (args.length > 2 && "list".equalsIgnoreCase(args[0]))
        {
            tag = args[1].toLowerCase();
            args = Arrays.copyOfRange(args, 2, args.length);
        }
        if (args.length == 1)
        {
            List<String> lst = new ArrayList<>();
            if ((tag != null) || (args[0].length() > 0))
            {
                for (String rule : CarpetSettings.toStringArray(CarpetSettings.find_all(tag)))
                {
                    lst.add(rule);
                }
            }
            lst.add("setDefault");
            lst.add("removeDefault");
            if (tag == null)
            {
                lst.add("defaults");
                lst.add("use");
                lst.add("list");
            }
            return getListOfStringsMatchingLastWord(args, lst.toArray(new String[0]));
        }
        if (args.length == 2)
        {
            if ("setDefault".equalsIgnoreCase(args[0]) || "removeDefault".equalsIgnoreCase(args[0]) )
            {
                return getListOfStringsMatchingLastWord(args, CarpetSettings.toStringArray(CarpetSettings.find_all(tag)));
            }
            return getListOfStringsMatchingLastWord(args, CarpetSettings.get(args[0]).getOptions());
        }
        if (args.length == 3 && "setDefault".equalsIgnoreCase(args[0]))
        {
            return getListOfStringsMatchingLastWord(args, CarpetSettings.get(args[1]).getOptions());
        }
        return Collections.<String>emptyList();
    }
}