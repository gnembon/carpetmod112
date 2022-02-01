package redstone.multimeter.common;

public enum TickTask {
	
	UNKNOWN              ( 0, "unknown"),
	TICK                 ( 1, "tick"),
	COMMAND_FUNCTIONS    ( 2, "command functions"),
	LEVELS               ( 3, "levels"),
	TICK_WORLD           ( 4, "tick world"),
	WORLD_BORDER         ( 5, "world border"),
	WEATHER              ( 6, "weather"),
	WAKE_SLEEPING_PLAYERS( 7, "wake sleeping players"),
	CHUNK_SOURCE         ( 8, "chunk source"),
	PURGE_UNLOADED_CHUNKS( 9, "purge unloaded chunks"),
	TICK_CHUNKS          (10, "tick chunks"),
	MOB_SPAWNING         (11, "mob spawning"),
	TICK_CHUNK           (12, "tick chunk"),
	THUNDER              (13, "thunder"),
	PRECIPITATION        (14, "precipitation"),
	RANDOM_TICKS         (15, "random ticks"),
	CUSTOM_MOB_SPAWNING  (16, "custom mob spawning"),
	BROADCAST_CHUNKS     (17, "broadcast chunks"),
	UNLOAD_CHUNKS        (18, "unload chunks"),
	CHUNK_MAP            (19, "chunk map"),
	TICK_TIME            (20, "tick time"),
	SCHEDULED_TICKS      (21, "scheduled ticks"),
	BLOCK_TICKS          (22, "block ticks"),
	FLUID_TICKS          (23, "fluid ticks"),
	VILLAGES             (24, "villages"),
	RAIDS                (25, "raids"),
	PORTALS              (26, "portals"),
	BLOCK_EVENTS         (27, "block events"),
	ENTITIES             (28, "entities"),
	REGULAR_ENTITIES     (29, "regular entities"),
	GLOBAL_ENTITIES      (30, "global entities"),
	PLAYERS              (31, "players"),
	DRAGON_FIGHT         (32, "dragon fight"),
	BLOCK_ENTITIES       (33, "block entities"),
	ENTITY_MANAGEMENT    (34, "entity management"),
	CONNECTIONS          (35, "connections"),
	PLAYER_PING          (36, "player ping"),
	SERVER_GUI           (37, "server gui"),
	AUTOSAVE             (38, "autosave"),
	PACKETS              (39, "packets");
	
	public static final TickTask[] ALL;
	
	static {
		ALL = new TickTask[values().length];
		
		for (TickTask task : values()) {
			ALL[task.index] = task;
		}
	}
	
	private final int index;
	private final String name;
	
	private TickTask(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static TickTask fromIndex(int index) {
		if (index > 0 && index < ALL.length) {
			return ALL[index];
		}
		
		return UNKNOWN;
	}
	
	public String getName() {
		return name;
	}
}
