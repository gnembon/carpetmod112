package carpet.logging;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CommandLogHandler extends LogHandler
{

    private String[] command;
    
    public CommandLogHandler(String... extraArgs)
    {
        this.command = extraArgs;
    }
    
    @Override
    public void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams)
    {
        if (commandParams == null) return;
        Map<String, String> params = paramsToMap(commandParams);
        String command = String.join(" ", this.command);
        for (Map.Entry<String, String> param : params.entrySet())
            command = command.replace("$" + param.getKey(), param.getValue());
        player.server.worldGenerationProgressListenerFactory.method_29374(player, command);
    }
    
    private static Map<String, String> paramsToMap(Object[] commandParams)
    {
        Map<String, String> params = new HashMap<>();
        
        if (commandParams.length % 2 != 0)
        {
            throw new IllegalArgumentException("commandParams.length must be even");
        }
        
        for (int i = 0; i < commandParams.length; i += 2)
        {
            if (!(commandParams[i] instanceof String))
            {
                throw new IllegalArgumentException("commandParams keys must be Strings");
            }
            params.put((String) commandParams[i], String.valueOf(commandParams[i + 1]));
        }
        
        return params;
    }
    
}
