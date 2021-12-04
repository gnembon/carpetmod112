package carpet.commands;

import carpet.utils.Data;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import test.AccurateTimer;

import java.util.ArrayList;
import java.util.TreeMap;

public class CommandPrint extends CommandCarpetBase {

    public static String[] messages = new String[]{
            "",//0
            "glass arrived",//1
            "reload chunk from queue start",//2
            "data input stream start",//3
            "data fixer start",//4
            "checked read start",//5
            "recreate structures start",//6
            "chunk insert start",//7
            "chunk loaded"//8
    };

    @Override
    public String getName() {
        return "printData";
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
        map.put(Data.reloadChunkFromQueueStartTime, 2);
        map.put(Data.dataInputStreamStart, 3);
        map.put(Data.dataFixerStart, 4);
        map.put(Data.checkedReadStart, 5);
        map.put(Data.recreateStructuresStart, 6);
        map.put(Data.chunkInsertStart, 7);
        map.put(Data.chunkLoadEnd, 8);
        System.out.println(accesses + " async chunk accesses");

        long minTime = 0;
        if(map.size() != 0){
            minTime = map.keySet().iterator().next();
        }

        String out = "\n__________";
        int asyncAccessCount = 0;
        for (long time : map.keySet()){
            int type = map.get(time);
            if(type==0){
                asyncAccessCount++;
            } else {
                if(asyncAccessCount!=0) {
                    out += "\n" + asyncAccessCount + " async accesses";
                }
                out += "\n" + messages[map.get(time)] + ": " + AccurateTimer.delta(minTime, time);
                asyncAccessCount = 0;
            }
        }
        if(asyncAccessCount!=0){
            out += "\n" + asyncAccessCount + " async accesses";
        }
        out += "\n__________";
        System.out.println(out);
    }
}
