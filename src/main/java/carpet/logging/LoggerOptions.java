package carpet.logging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.IJsonSerializable;

public class LoggerOptions implements IJsonSerializable {
    public String logger;
    public String option;
    public String handlerName;
    public String[] extraArgs;

    LoggerOptions() {}

    public LoggerOptions(String logger, String option, String handlerName, String... extraArgs) {
        this.logger = logger;
        this.option = option;
        this.handlerName = handlerName;
        this.extraArgs = extraArgs;
    }

    @Override
    public void fromJson(JsonElement json) {
        JsonObject obj = (JsonObject)json;

        logger = obj.get("logger").getAsString();
        if (!obj.get("option").isJsonNull())
            option = obj.get("option").getAsString();
        if (!obj.get("handlerName").isJsonNull())
            handlerName = obj.get("handlerName").getAsString();

        JsonArray args = obj.getAsJsonArray("extraArgs");
        extraArgs = new String[args.size()];
        for (int i = 0; i < extraArgs.length; ++i) {
            extraArgs[i++] = args.get(i).getAsString();
        }
    }

    @Override
    public JsonElement getSerializableElement() {
        JsonObject entry = new JsonObject();

        entry.addProperty("logger", logger);
        entry.addProperty("option", option);
        entry.addProperty("handlerName", handlerName);

        JsonArray args = new JsonArray();
        for (String extraArg : this.extraArgs) {
            args.add(extraArg);
        }
        entry.add("extraArgs", args);

        return entry;
    }
}
