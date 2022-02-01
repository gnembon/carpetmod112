package redstone.multimeter.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import redstone.multimeter.RedstoneMultimeter;
import redstone.multimeter.util.NbtUtils;

public class TickPhaseTree {
	
	public final TickTaskNode root;
	
	private TickTaskNode current;
	private boolean building;
	private boolean complete;
	
	public TickPhaseTree() {
		this.root = new TickTaskNode(null, TickTask.UNKNOWN);
		
		this.current = root;
		this.building = false;
		this.complete = false;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public boolean isBuilding() {
		return building;
	}
	
	public void start() {
		if (building) {
			RedstoneMultimeter.LOGGER.warn("Cannot start building tick phase tree: already building!");
		} else {
			root.children.clear();
			current = root;
			building = true;
			complete = false;
		}
	}
	
	public void end() {
		if (building) {
			building = false;
			complete = true;
		} else {
			RedstoneMultimeter.LOGGER.warn("Cannot complete tick phase tree: not building!");
		}
	}
	
	public void startTask(TickTask task, String... args) {
		if (building) {
			current = new TickTaskNode(current, task, args);
			current.parent.children.add(current);
		}
	}
	
	public void endTask() {
		if (building) {
			current = current.parent;
			
			if (current == null) {
				current = root; // we should never get here
			}
		}
	}
	
	public void swapTask(TickTask task, String... args) {
		if (building) {
			endTask();
			startTask(task, args);
		}
	}
	
	public NBTTagCompound toNbt() {
		NBTTagList tasks = new NBTTagList();
		NBTTagList args = new NBTTagList();
		
		addNode(tasks, args, root, 0);
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("tasks", tasks);
		nbt.setTag("args", args);
		
		return nbt;
	}
	
	private void addNode(NBTTagList tasks, NBTTagList args, TickTaskNode node, int depth) {
		if (depth > 0) { // depth 0 is root
			byte[] array = new byte[3];
			array[0] = (byte)depth;
			array[1] = (byte)node.task.getIndex();
			array[2] = (byte)node.args.length;
			NBTTagByteArray taskNbt = new NBTTagByteArray(array);
			
			tasks.appendTag(taskNbt);
			
			for (int index = 0; index < node.args.length; index++) {
				String arg = node.args[index];
				NBTTagString argNbt = new NBTTagString(arg);
				
				args.appendTag(argNbt);
			}
		}
		
		depth++;
		
		for (int index = 0; index < node.children.size(); index++) {
			addNode(tasks, args, node.children.get(index), depth);
		}
	}
	
	public void fromNbt(NBTTagCompound nbt) {
		NBTTagList tasks = nbt.getTagList("tasks", NbtUtils.TYPE_BYTE_ARRAY);
		NBTTagList args = nbt.getTagList("args", NbtUtils.TYPE_STRING);
		
		if (!tasks.isEmpty()) {
			start();
			addNode(tasks, args, 0, 0, 0);
			end();
		}
	}
	
	private void addNode(NBTTagList tasks, NBTTagList args, int taskIndex, int argIndex, int lastDepth) {
		NBTTagByteArray taskNbt = (NBTTagByteArray)tasks.get(taskIndex);
		byte[] array = taskNbt.getByteArray();
		int depth = array[0];
		TickTask task = TickTask.fromIndex(array[1]);
		int argsLength = array[2];
		
		String[] taskArgs;
		
		if (argsLength > 0) {
			taskArgs = new String[argsLength];
			
			for (int i = 0; i < argsLength && argIndex < args.tagCount(); ) {
				taskArgs[i++] = args.getStringTagAt(argIndex++);
			}
		} else {
			taskArgs = new String[0];
		}
		
		int endedTasks = lastDepth - depth;
		
		while (endedTasks-- > 0) {
			endTask();
		}
		if (depth > lastDepth) {
			startTask(task, taskArgs);
		} else {
			swapTask(task, taskArgs);
		}
		
		if (++taskIndex < tasks.tagCount()) {
			addNode(tasks, args, taskIndex, argIndex, depth);
		}
	}
	
	public class TickTaskNode {
		
		public final TickTaskNode parent;
		public final List<TickTaskNode> children;
		public final TickTask task;
		public final String[] args;
		
		public TickTaskNode(TickTaskNode parent, TickTask task, String... args) {
			this.parent = parent;
			this.children = new ArrayList<>();
			this.task = task;
			this.args = args;
		}
	}
}
