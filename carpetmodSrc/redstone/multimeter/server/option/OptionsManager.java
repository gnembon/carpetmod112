package redstone.multimeter.server.option;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import redstone.multimeter.common.meter.event.EventType;

public class OptionsManager {
	
	private static final String FILE_NAME = "options.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	public static Options load(File folder) {
		File file = new File(folder, FILE_NAME);
		return validate(file.exists() ? read(file) : write(file));
	}
	
	private static Options read(File file) {
		try (FileReader fr = new FileReader(file)) {
			return GSON.fromJson(fr, Options.class);
		} catch (IOException e) {
			return new Options();
		}
	}
	
	private static Options write(File file) {
		Options options = new Options();
		
		try (FileWriter fw = new FileWriter(file)) {
			fw.write(GSON.toJson(options));
		} catch (IOException e) {
			
		}
		
		return options;
	}
	
	private static Options validate(Options options) {
		switch (options.event_types.allowed) {
		case "blacklist":
			for (String name : options.event_types.blacklist) {
				EventType type = EventType.fromName(name);
				
				if (type != null) {
					options.enabledEventTypes &= ~type.flag();
				}
			}
			
			break;
		case "whitelist":
			options.enabledEventTypes = 0;
			
			for (String name : options.event_types.whitelist) {
				EventType type = EventType.fromName(name);
				
				if (type != null) {
					options.enabledEventTypes |= type.flag();
				}
			}
			
			break;
		}
		
		return options;
	}
}
