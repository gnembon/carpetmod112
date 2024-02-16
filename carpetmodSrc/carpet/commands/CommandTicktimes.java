package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandTicktimes extends CommandCarpetBase {
    private static final String DEFAULT_NAME = "ticktimes.txt";
    public static boolean logging;

    public static LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>();
    private static File file;
    private static WriteThread writeThread;

    /**
     * Gets the name of the command
     */
    public String getUsage(ICommandSender sender) {
        return "/ticktimes <start|stop> [filename]";
    }

    public String getName() {
        return "ticktimes";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandTicktimes", sender)) return;

        if (args.length == 0) throw new WrongUsageException(getUsage(sender));
        try {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "start":
                    String name = DEFAULT_NAME;
                    if (args.length == 2) name = args[1];
                    if (logging) throw new CommandException(String.format("Already logging to %s", file.getName()));

                    file = new File(name);
                    if (file.createNewFile()) {
                        notifyCommandListener(sender, this, String.format("Creating file %s", name));
                    } else {
                        notifyCommandListener(sender, this, String.format("File %s already exists, appending to file", name));
                    }
                    writeThread = new WriteThread(server);
                    writeThread.start();
                    writeThread.setPriority(Thread.MIN_PRIORITY);

                    notifyCommandListener(sender, this, String.format("Logging ticktimes to %s", name));
                    return;
                case "stop":
                    if (logging) {
                        notifyCommandListener(sender, this, String.format("Stopped logging ticktimes to %s", file.getName()));
                        writeThread.finish();
                    } else {
                        notifyCommandListener(sender, this, "Not currently logging");
                    }
                    return;
            }
        } catch (IOException e) {
            throw new CommandException("Error logging to file");
        }
        throw new WrongUsageException(getUsage(sender));

    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (!CarpetSettings.commandTicktimes) return Collections.emptyList();

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "start", "stop");
        }
        return Collections.emptyList();
    }

    static class WriteThread extends Thread {
        private final MinecraftServer server;

        WriteThread(MinecraftServer server) {
            this.server = server;
        }

        public void run() {
            queue.clear();
            logging = true;
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true), true)) {
                long time;
                while (true) {
                    time = CommandTicktimes.queue.take();
                    if (time == -1) break;
                    writer.println(time);
                }
            } catch (InterruptedException | IOException ignored) {
                Messenger.print_server_message(server, Messenger.m(null, "r Error logging ticktimes, logging has stopped"));
                this.finish();
            }
        }

        public void finish() {
            logging = false;
            file = null;
            writeThread = null;
            queue.offer(-1L);
        }
    }
}