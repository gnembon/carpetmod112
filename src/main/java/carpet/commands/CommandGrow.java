package carpet.commands;

import net.minecraft.class_6182;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandGrow extends CommandCarpetBase {
    private static final ItemStack STACK = new ItemStack(Items.DYE, 1, DyeColor.WHITE.getFireworkColor());

    @Override
    public String method_29277() {
        return "grow";
    }

    @Override
    public String method_29275(CommandSource sender) {
        return this.method_29277() + " <x> <y> <z> [times]";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if(!command_enabled("commandGrow", sender)) return;

        if (args.length < 3) {
            throw new class_6182(this.method_29275(sender));
        }

        final int amount = args.length > 3 ? method_28715(args[3]) : 1;
        final BlockPos pos = method_28713(sender, args, 0, false);
        final World world = sender.getEntityWorld();
        if (!world.canSetBlock(pos)) {
//            throw new class_6175("Position is not loaded!");
        } else {
            for (int i = 0; i < amount; ++i) {
                DyeItem.method_25442(STACK, world, pos);
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
        if (args.length > 0 && args.length <= 3) {
            return method_28730(args, 0, pos);
        }

        return Collections.emptyList();
    }
}