package carpet.utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.minecraft.command.CommandException;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Chunklogger {
	
	static Gson gson = new Gson();
	static final String logdir = "chunklog";
	
	enum Status {
		@SerializedName("u") UNLOADED, 
		@SerializedName("p") PLAYERLOADED,
		@SerializedName("l") LOADED,
		@SerializedName("q") UNLOADQUEUED,
		@SerializedName("Q") UNLOADQUEUEING, 
		@SerializedName("U") UNLOADING,
		@SerializedName("C") UNLOADINGCANCELED,
		@SerializedName("L") LOADING;
	}
	
	class ChunkLogEntry {
		Status s;
		String info;
	}
	
	int x0, z0, dx, dz;
	ChunkLogEntry[] entries;
	
	private transient String name;
	transient boolean modified;

	boolean contains(int x, int z) {
		if(x<x0 || z<z0) {
			return false;
		}
		x-= x0;
		z-= z0;
		return x<dx  && z<dz;
	}
	
	int index(int x, int z) {
		x-= x0;
		z-= z0;
		return x+z*dx;
	}
	
	public Chunklogger(World w, int cx1, int cz1, int cx2, int cz2, String name) throws CommandException{
		
		Path path = Paths.get(".",logdir, name);
		if(Files.exists(path)) {
			throw new CommandException("Recording '"+name+"' already exists. Delete old recording or use a new name");
		}
		else {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				throw new IllegalArgumentException("Cannot create directory");
			}
		}
		
		x0 = cx1<cx2?cx1:cx2;
		z0 = cz1<cz2?cz1:cz2;
		int x1 = cx1>cx2?cx1:cx2;
		int z1 = cz1>cz2?cz1:cz2;
		dx = x1-x0+1;
		dz = z1-z0+1;
		entries = new ChunkLogEntry[dx*dz];
		for(int zz=0; zz<dz; ++zz) {
			for(int xx=0; xx<dx; ++xx) {
				int x = x0+xx;
				int z = z0+zz;
				int i = xx+zz*dx;
				entries[i] = new ChunkLogEntry();
			}
		}
		clearEntries(w);
		w.chunkloggers.put(name, this);
		this.name = name;
	}
	
	void clearEntries(World ww) {
		modified = false;
		WorldServer w = (WorldServer) ww;
		ChunkProviderServer cp = (ChunkProviderServer)(w.getChunkProvider());
		PlayerChunkMap chunkmap = w.getPlayerChunkMap();
		for(int zz=0; zz<dz; ++zz) {
			for(int xx=0; xx<dx; ++xx) {
				int x = x0+xx;
				int z = z0+zz;
				int i = xx+zz*dx;
				entries[i].info = null;
				Status olds = entries[i].s;
				if(cp.chunkExists(x, z)) {
					entries[i].s = Status.LOADED;
					if(cp.isChunkUnloadScheduled(x, z)) {
						entries[i].s = Status.UNLOADQUEUED;
					} else if(chunkmap.contains(x, z)) {
						entries[i].s = Status.PLAYERLOADED;
					}
				}
				else {
					entries[i].s = Status.UNLOADED;
				}
				if(olds != entries[i].s) {
					modified = true;
				}
			}
		}
	}
	
	void dumpLog(World w) {
		if(!modified) {
			return;
		}
		int gameticks = w.getMinecraftServer().getTickCounter();
		String s = gson.toJson(this);
		
		Path file = Paths.get(".",logdir, name , "new.json");
		try {
			File f = Files.createFile(file).toFile();
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f,false), StandardCharsets.UTF_8);
			writer.write(s);
			writer.flush();
			writer.close();
			Path file2 = Paths.get(".",logdir, name, ""+ gameticks + ".json");
			Files.move(file, file2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static String getExecutionOrigin() {
		StackTraceElement[] trace = new Throwable().getStackTrace();
		String result = new String();
		for(StackTraceElement e:trace) {
			String file = e.getFileName();
			if("Chunklogger.java".equals(file))
				continue;
			if(!result.isEmpty()) {
				result += System.lineSeparator();
			}
			String cl = e.getClassName();
			String me = e.getMethodName();
			int ln = e.getLineNumber();
			result += cl + "." + me + "(" + file +":" + ln + ")";
		}
		return result;
	}
	
	void log(int x, int z, Status s) {
		if(!contains(x,z))
			return;
		modified = true;
		int i = index(x,z);
		String newinfo = s.toString() + System.lineSeparator() + getExecutionOrigin();
		if(entries[i].info!= null) {
			entries[i].info += System.lineSeparator() + System.lineSeparator() + newinfo;
		}
		else {
			entries[i].info = newinfo;
		}
		entries[i].s = s;
	}
	
	public static void clearAll(World w) {
		for(Chunklogger l: w.chunkloggers.values()) {
			l.clearEntries(w);
		}
	}
	
	public static void logAll(World w) {
		for(Chunklogger l: w.chunkloggers.values()) {
			l.dumpLog(w);
		}
	}
	
	public static void logChunkLoading(World w, int x, int z) {
		for(Chunklogger l: w.chunkloggers.values()) {
			l.log(x,z,Status.LOADING);
		}
	}
	public static void logChunkUnloading(World w, int x, int z) {
		for(Chunklogger l: w.chunkloggers.values()) {
			l.log(x,z,Status.UNLOADING);
		}
	}
	public static void logChunkQueued(World w, int x, int z) {
		for(Chunklogger l: w.chunkloggers.values()) {
			l.log(x,z,Status.UNLOADQUEUEING);
		}
	}
	
	public static void logChunkUnloadingCanceled(World w, int x, int z) {
		for(Chunklogger l: w.chunkloggers.values()) {
			l.log(x,z,Status.UNLOADINGCANCELED);
		}
	}
}
