package carpet.commands;

import carpet.CarpetServer;
import narcolepticfrog.rsmm.MeterCommand;
import net.minecraft.class_2002;

public class CarpetCommands {
    public static void register(class_2002 handler) {
        // Sorted alphabetically to make merge conflicts less likely
        handler.method_29056(new CommandAutosave());
        handler.method_29056(new CommandBlockInfo());
        handler.method_29056(new CommandCarpet());
        handler.method_29056(new CommandCounter());
        handler.method_29056(new CommandDebugCarpet());
        handler.method_29056(new CommandDebuglogger());
        handler.method_29056(new CommandDistance());
        handler.method_29056(new CommandEntityInfo());
        handler.method_29056(new CommandFillBiome());
        handler.method_29056(new CommandGMC());
        handler.method_29056(new CommandGMS());
        handler.method_29056(new CommandGrow());
        handler.method_29056(new CommandLagSpike());
        handler.method_29056(new CommandLight());
        handler.method_29056(new CommandLog());
        handler.method_29056(new CommandPerimeter());
        handler.method_29056(new CommandPing());
        handler.method_29056(new CommandPlayer());
        handler.method_29056(new CommandProfile());
        handler.method_29056(new CommandRemoveEntity());
        handler.method_29056(new CommandRepopulate());
        handler.method_29056(new CommandRNG());
        handler.method_29056(new CommandScoreboardPublic());
        handler.method_29056(new CommandSpawn());
        handler.method_29056(new CommandStructure());
        handler.method_29056(new CommandSubscribe());
        handler.method_29056(new CommandTick());
        handler.method_29056(new CommandTickingArea());
        handler.method_29056(new CommandTNT());
        handler.method_29056(new CommandUnload());
        handler.method_29056(new CommandUnload13());
        handler.method_29056(new CommandUpdateCarpet());
        handler.method_29056(new CommandWaypoint());

        // ----- RSMM Start ----- //
        handler.method_29056(new MeterCommand(CarpetServer.rsmmServer));
        // ----- RSMM End ----- //
    }
}
