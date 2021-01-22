package carpet.commands;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import carpet.mixin.accessors.StructureManagerAccessor;
import org.apache.logging.log4j.LogManager;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.class_6182;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.LiteralText;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class CommandStructure extends CommandCarpetBase
{

    private static final String USAGE = "/structure <load|save|list> ...";
    private static final String USAGE_LOAD = "/structure load <name> [pos: x y z] [mirror] [rotation] [ignoreEntities] [integrity] [seed]";
    private static final String USAGE_SAVE = "/structure save <name> <from: x y z> <to: x y z> [ignoreEntities]";
    
    @Override
    public String method_29277()
    {
        return "structure";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return USAGE;
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandStructure", sender))
            return;
        
        if (args.length < 1)
            throw new class_6182(USAGE);
        
        switch (args[0])
        {
        case "load":
            loadStructure(server, sender, args);
            break;
        case "save":
            saveStructure(server, sender, args);
            break;
        case "list":
            listStructure(server, sender, args);
            break;
        default:
            throw new class_6182(USAGE);
        }
    }
    
    private void loadStructure(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw new class_6182(USAGE_LOAD);
        
        args = replaceQuotes(args);
        
        String structureName = args[1];

        for (char illegal : SharedConstants.INVALID_CHARS_IDENTIFIER)
            structureName = structureName.replace(illegal, '_');
        StructureManager manager = server.worlds[0].getStructureManager();
        Structure template = manager.method_27994(server, new Identifier(structureName));
        if (template == null)
            throw new CommandException("Template \"" + args[1] + "\" doesn't exist");
        
        BlockPos origin = sender.getBlockPos();
        if (args.length >= 5)
        {
            origin = method_28713(sender, args, 2, false);
        }
        
        BlockMirror mirror = BlockMirror.NONE;
        if (args.length >= 6)
        {
            switch (args[5])
            {
            case "no_mirror":
                mirror = BlockMirror.NONE;
                break;
            case "mirror_left_right":
                mirror = BlockMirror.LEFT_RIGHT;
                break;
            case "mirror_front_back":
                mirror = BlockMirror.FRONT_BACK;
                break;
            default:
                throw new CommandException("Unknown mirror: " + args[5]);
            }
        }
        
        BlockRotation rotation = BlockRotation.NONE;
        if (args.length >= 7)
        {
            switch (args[6])
            {
            case "rotate_0":
                rotation = BlockRotation.NONE;
                break;
            case "rotate_90":
                rotation = BlockRotation.CLOCKWISE_90;
                break;
            case "rotate_180":
                rotation = BlockRotation.CLOCKWISE_180;
                break;
            case "rotate_270":
                rotation = BlockRotation.COUNTERCLOCKWISE_90;
                break;
            default:
                throw new CommandException("Unknown rotation: " + args[6]);
            }
        }
        
        boolean ignoreEntities = true;
        if (args.length >= 8)
        {
            ignoreEntities = method_28744(args[7]);
        }
        
        float integrity = 1;
        if (args.length >= 9)
        {
            integrity = (float) method_28717(args[8], 0, 1);
        }
        
        long seed;
        if (args.length >= 10)
        {
            seed = method_28738(args[9]);
        }
        else
        {
            seed = new Random().nextLong();
        }
        
        StructurePlacementData settings = new StructurePlacementData().setMirrored(mirror).setRotation(rotation).setIgnoreEntities(ignoreEntities).method_28000(null).method_28001(null).method_28011(false);
        if (integrity < 1)
        {
            settings.method_27999(integrity).method_28006(seed);
        }
        
        template.place(sender.getEntityWorld(), origin, settings);
        
        method_28710(sender, this, "Successfully loaded structure " + args[1]);
    }
    
    private void saveStructure(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (args.length < 8)
            throw new class_6182(USAGE_SAVE);
        
        args = replaceQuotes(args);
        
        BlockPos pos1 = method_28713(sender, args, 2, false);
        BlockPos pos2 = method_28713(sender, args, 5, false);
        
        BlockBox bb = new BlockBox(pos1, pos2);
        BlockPos origin = new BlockPos(bb.minX, bb.minY, bb.minZ);
        BlockPos size = new BlockPos(bb.getBlockCountX(), bb.getBlockCountY(), bb.getBlockCountZ());
        
        boolean ignoreEntities = true;
        if (args.length >= 9)
        {
            ignoreEntities = method_28744(args[8]);
        }
        
        String structureName = args[1];
        for (char illegal : SharedConstants.INVALID_CHARS_IDENTIFIER)
            structureName = structureName.replace(illegal, '_');
        StructureManager manager = server.worlds[0].getStructureManager();
        Structure template = manager.method_27992(server, new Identifier(structureName));
        template.method_28026(sender.getEntityWorld(), origin, size, !ignoreEntities, Blocks.STRUCTURE_VOID);
        template.setAuthor(sender.getName());
        manager.method_27996(server, new Identifier(structureName));
        
        method_28710(sender, this, "Successfully saved structure " + structureName);
    }
    
    private void listStructure(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        StructureManager manager = server.worlds[0].getStructureManager();
        List<String> templates = listStructures(manager);
        
        if (templates.isEmpty())
        {
            sender.sendSystemMessage(new LiteralText("There are no saved structures yet"));
        }
        else
        {
            final int PAGE_SIZE = 9;
            int pageCount = (templates.size() + PAGE_SIZE - 1) / PAGE_SIZE;
            int page = args.length >= 2 ? method_28715(args[1]) - 1 : 0;
            page = MathHelper.clamp(page, 0, pageCount - 1);
            
            sender.sendSystemMessage(new LiteralText(Formatting.GREEN + "Structure list page " + (page + 1) + " of " + pageCount + " (/structure list <page>)"));
            for (int offset = 0; offset < PAGE_SIZE && page * PAGE_SIZE + offset < templates.size(); offset++)
            {
                String template = templates.get(page * PAGE_SIZE + offset);
                sender.sendSystemMessage(new LiteralText("- " + template + " by " + manager.method_27994(server, new Identifier(template)).getAuthor()));
            }
        }
    }
    
    private static List<String> listStructures(StructureManager manager)
    {
        List<String> templates = new ArrayList<>();
        
        Path baseFolder = Paths.get(((StructureManagerAccessor) manager).getBaseFolder());
        try
        {
            if (Files.exists(baseFolder))
            {
                Files.find(baseFolder, Integer.MAX_VALUE,
                        (path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(".nbt"))
                .forEach(path -> templates.add(baseFolder.relativize(path).toString().replace(File.separator, "/").replace(".nbt", "")));
            }
        }
        catch (IOException e)
        {
            LogManager.getLogger().error("Unable to list custom structures", e);
        }

        Pattern pattern = Pattern.compile(".*assets/(\\w+)/structures/([\\w/]+)\\.nbt");
        try
        {
            try
            {
                // try zip file first
                ZipFile zip = new ZipFile(new File(MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements())
                {
                    String entryName = entries.nextElement().getName();
                    Matcher matcher = pattern.matcher(entryName);
                    if (matcher.matches())
                    {
                        templates.add(matcher.group(1) + ":" + matcher.group(2));
                    }
                }
                zip.close();
            }
            catch (Exception e)
            {
                // try folder
                Path root = Paths.get(MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                Files.find(root, Integer.MAX_VALUE,
                        (path, attr) -> attr.isRegularFile() && pattern.matcher(path.toString().replace(File.separator, "/")).matches())
                .forEach(path -> {
                    Matcher matcher = pattern.matcher(path.toString().replace(File.separator, "/"));
                    matcher.matches(); // == true
                    templates.add(matcher.group(1) + ":" + matcher.group(2));
                });
            }
        }
        catch (IOException | URISyntaxException e)
        {
            LogManager.getLogger().error("Unable to list built in structures", e);
        }
        
        Collections.sort(templates);
        
        return templates;
    }
    
    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return method_28732(args, "load", "save", "list");
        }
        else if ("load".equals(args[0]) || "save".equals(args[0]))
        {
            if (args[1].startsWith("\""))
            {
                boolean replaced = false;
                try
                {
                    if (!args[args.length - 1].endsWith("\""))
                    {
                        args = replaceQuotes(args);
                        replaced = true;
                    }
                }
                catch (CommandException e)
                {
                }
                if (!replaced)
                {
                    String commonPrefix = Arrays.stream(args).skip(1).limit(args.length - 2).collect(Collectors.joining(" "));
                    List<String> structs = listStructures(server.worlds[0].getStructureManager());
                    structs = structs.stream().map(s -> "\"" + s + "\"").collect(Collectors.toList());
                    if (!commonPrefix.isEmpty())
                        structs = structs.stream().filter(s -> s.startsWith(commonPrefix + " ")).map(s -> s.substring(commonPrefix.length() + 1)).collect(Collectors.toList());
                    structs = structs.stream().map(s -> s.split(" ")[0]).collect(Collectors.toList());
                    return method_28731(args, structs);
                }
            }
            else if (args.length == 2)
            {
                return method_28731(args, listStructures(server.worlds[0].getStructureManager()).stream().filter(s -> !s.contains(" ")).collect(Collectors.toList()));
            }
            
            if (args.length >= 3 && args.length <= 5)
            {
                return method_28730(args, 2, targetPos);
            }
            else if ("load".equals(args[0]))
            {
                if (args.length == 6)
                {
                    return method_28732(args, "no_mirror", "mirror_left_right", "mirror_front_back");
                }
                else if (args.length == 7)
                {
                    return method_28732(args, "rotate_0", "rotate_90", "rotate_180", "rotate_270");
                }
                else if (args.length == 8)
                {
                    return method_28732(args, "true", "false");
                }
                else
                {
                    return Collections.emptyList();
                }
            }
            else
            {
                if (args.length >= 6 && args.length <= 8)
                {
                    return method_28730(args, 5, targetPos);
                }
                else if (args.length == 9)
                {
                    return method_28732(args, "true", "false");
                }
                else
                {
                    return Collections.emptyList();
                }
            }
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
    private static String[] replaceQuotes(String[] args) throws CommandException
    {
        String structureName = args[1];
        if (structureName.startsWith("\""))
        {
            int i = 2;
            while (!structureName.endsWith("\"") && i < args.length) {
                structureName += " " + args[i++];
            }
            if (!structureName.endsWith("\""))
                throw new CommandException("Unbalanced \"\" quotes");
            structureName = structureName.substring(1, structureName.length() - 1);
            String[] newArgs = new String[args.length - (i - 2)];
            newArgs[0] = args[0];
            newArgs[1] = structureName;
            System.arraycopy(args, i, newArgs, 2, newArgs.length - 2);
            return newArgs;
        }
        else
        {
            return args;
        }
    }

}
