package carpet.commands;

import carpet.utils.Data;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import test.AccurateTimer;

import java.util.ArrayList;
import java.util.TreeMap;

public class CommandBeaconPrint extends CommandCarpetBase {

    public static String[] messages = new String[]{
            "",//0
            "glass arrived",//1
            "reload chunk from queue start",//2
            "data input stream start",//3
            "data fixer start",//4
            "checked read start",//5
            "recreate structures start",//6
            "chunk insert start",//7
            "chunk loaded",//8
            "chunk removed"//9
    };

    @Override
    public String getName() {
        return "beaconPrintData";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        System.out.println("clearconsole");
        long accesses = 0;
        TreeMap<Long, Integer> map = new TreeMap<>();
        for(ArrayList<Long> list : Data.times){
            for (long time : list){
                map.put(time, 0);
            }
            accesses+=list.size();
        }
        Data.times.clear();

        for(Long l : Data.glassArrivalTimes){
            map.put(l, 1);
        }
        Data.glassArrivalTimes.clear();

        map.put(Data.removeEnd, 9);

        int glassCounter = 0;
        String out = "\n";
        int count = 0;

        long minTime = 0;
        if(map.size() != 0){
            minTime = map.keySet().iterator().next();
        }

        for (long time : map.keySet()) {
            int type = map.get(time);
            if (type == 0) {
                glassCounter++;
                if(count > 0){
                    out = out + "Glass time: " + AccurateTimer.delta(minTime, time) + "\n";
                    count--;
                }
            } else if (type == 9) {
                out = out + "Glass before: " + glassCounter + "\n";
                out = out + "Removed time: " + AccurateTimer.delta(minTime, Data.removeEnd) + "\n";
                count = 3;
                glassCounter = 0;
            }
        }
        out = out + "Glass after: " + glassCounter + "\n";
        out = out + "Remove delta: " + (AccurateTimer.delta(minTime, Data.removeEnd) - AccurateTimer.delta(minTime, Data.removeStart));
        System.out.println(out);
    }
}
