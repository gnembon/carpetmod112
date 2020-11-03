package carpet.commands;

import carpet.CarpetSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandGrow extends CommandCarpetBase {
    private static final ItemStack STACK = new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage());

    @Override
    public String getName() {
        return "grow";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return this.getName() + " <x> <y> <z> [times]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!command_enabled("commandGrow", sender)) return;

        if (args.length < 3) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        final int amount = args.length > 3 ? parseInt(args[3]) : 1;
        final BlockPos pos = parseBlockPos(sender, args, 0, false);
        final World world = sender.getEntityWorld();
        if (!world.isBlockLoaded(pos)) {
//            throw new CommandException("Position is not loaded!");
        } else {
            for (int i = 0; i < amount; ++i) {
                ItemDye.applyBonemeal(STACK, world, pos);
            }
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length > 0 && args.length <= 3) {
            return getTabCompletionCoordinate(args, 0, pos);
        }

        return Collections.emptyList();
    }
}