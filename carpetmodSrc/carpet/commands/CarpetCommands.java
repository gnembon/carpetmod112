package carpet.commands;

import carpet.CarpetServer;
import narcolepticfrog.rsmm.MeterCommand;
import net.minecraft.command.CommandHandler;
import redstone.multimeter.command.MeterGroupCommand;

public class CarpetCommands {
    public static void register(CommandHandler handler) {
        // Sorted alphabetically to make merge conflicts less likely
        // For Xcom: A B C D E F G H I J K L M N O P Q R S T U V W X Y Z

        handler.registerCommand(new CommandAutosave());
        handler.registerCommand(new CommandBlockInfo());
        handler.registerCommand(new CommandCarpet());
        handler.registerCommand(new CommandChunk());
        handler.registerCommand(new CommandColon());
        handler.registerCommand(new CommandCounter());
        handler.registerCommand(new CommandDebugCarpet());
        handler.registerCommand(new CommandDebuglogger());
        handler.registerCommand(new CommandDistance());
        handler.registerCommand(new CommandEntityInfo());
        handler.registerCommand(new CommandFeel());
        handler.registerCommand(new CommandFillBiome());
        handler.registerCommand(new CommandGMC());
        handler.registerCommand(new CommandGMS());
        handler.registerCommand(new CommandGrow());
        handler.registerCommand(new CommandLagSpike());
        handler.registerCommand(new CommandLazyChunkBehavior());
        handler.registerCommand(new CommandLight());
        handler.registerCommand(new CommandLoadedChunks());
        handler.registerCommand(new CommandLog());
        handler.registerCommand(new CommandPalette());
        handler.registerCommand(new CommandPerimeter());
        handler.registerCommand(new CommandPing());
        handler.registerCommand(new CommandPlayer());
        handler.registerCommand(new CommandProfile());
        handler.registerCommand(new CommandRemoveEntity());
        handler.registerCommand(new CommandRepopulate());
        handler.registerCommand(new CommandRNG());
        handler.registerCommand(new CommandScoreboardPublic());
        handler.registerCommand(new CommandSpawn());
        handler.registerCommand(new CommandStructure());
        handler.registerCommand(new CommandSubscribe());
        handler.registerCommand(new CommandTick());
        handler.registerCommand(new CommandTickingArea());
        handler.registerCommand(new CommandTNT());
        handler.registerCommand(new CommandUnload());
        handler.registerCommand(new CommandUnload13());
        handler.registerCommand(new CommandUpdateCarpet());
        handler.registerCommand(new CommandWaypoint());
        handler.registerCommand(new CommandZetBlock());

        // ----- RSMM Start ----- //
        handler.registerCommand(new MeterCommand(CarpetServer.legacyRsmmServer));
        // ----- RSMM End ----- //

        handler.registerCommand(new MeterGroupCommand(CarpetServer.rsmmServer));
    }
}
