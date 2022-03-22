package redstone.multimeter.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import redstone.multimeter.RedstoneMultimeter;
import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;
import redstone.multimeter.server.meter.ServerMeterGroup;

public class MeterGroupCommand extends CommandBase {
	
	private static final String COMMAND_NAME = "metergroup";
	
	private static final String USAGE_LIST              = singleUsage("list");
	
	private static final String USAGE_SUBSCRIBE_DEFAULT = singleUsage("subscribe");
	private static final String USAGE_SUBSCRIBE_NAME    = singleUsage("subscribe <name>");
	private static final String USAGE_SUBSCRIBE         = buildUsage(USAGE_SUBSCRIBE_DEFAULT, USAGE_SUBSCRIBE_NAME);
	
	private static final String USAGE_UNSUBSCRIBE       = singleUsage("unsubscribe");
	
	private static final String USAGE_PRIVATE_QUERY     = singleUsage("private");
	private static final String USAGE_PRIVATE_SET       = singleUsage("private <private true|false>");
	private static final String USAGE_PRIVATE           = buildUsage(USAGE_PRIVATE_QUERY, USAGE_PRIVATE_SET);
	
	private static final String USAGE_MEMBERS_LIST      = singleUsage("members list");
	private static final String USAGE_MEMBERS_ADD       = singleUsage("members add <player>");
	private static final String USAGE_MEMBERS_REMOVE    = singleUsage("members remove <player>");
	private static final String USAGE_MEMBERS_CLEAR     = singleUsage("members clear");
	private static final String USAGE_MEMBERS           = buildUsage(USAGE_MEMBERS_LIST, USAGE_MEMBERS_ADD, USAGE_MEMBERS_REMOVE, USAGE_MEMBERS_CLEAR);
	
	private static final String USAGE_CLEAR             = singleUsage("clear");
	
	private static final String TOTAL_USAGE_MEMBER = buildUsage(USAGE_LIST, USAGE_SUBSCRIBE, USAGE_UNSUBSCRIBE, USAGE_CLEAR);
	private static final String TOTAL_USAGE_OWNER  = buildUsage(USAGE_LIST, USAGE_SUBSCRIBE, USAGE_UNSUBSCRIBE, USAGE_PRIVATE, USAGE_MEMBERS, USAGE_CLEAR);
	
	private static String singleUsage(String usage) {
		return String.format("/%s %s", COMMAND_NAME, usage);
	}
	
	private static String buildUsage(String... usages) {
		return String.join(" OR ", usages);
	}
	
	private final MultimeterServer server;
	private final Multimeter multimeter;
	
	public MeterGroupCommand(MultimeterServer server) {
		this.server = server;
		this.multimeter = this.server.getMultimeter();
	}
	
	@Override
	public String getName() {
		return COMMAND_NAME;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		try {
			if (isOwnerOfSubscription(sender)) {
				return TOTAL_USAGE_OWNER;
			}
		} catch (CommandException e) {
			
		}
		
		return TOTAL_USAGE_MEMBER;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer minecraftServer, ICommandSender sender, String[] args, BlockPos pos) {
		boolean isOwner = false;
		
		try {
			isOwner = isOwnerOfSubscription(sender);
		} catch (CommandException e) {
			
		}
		
		switch (args.length) {
		case 1:
			if (isOwner) {
				return getListOfStringsMatchingLastWord(args, "clear", "subscribe", "unsubscribe", "private", "members", "list");
			} else {
				return getListOfStringsMatchingLastWord(args, "clear", "subscribe", "unsubscribe", "list");
			}
		case 2:
			switch (args[0]) {
			case "subscribe":
				try {
					return getListOfStringsMatchingLastWord(args, listMeterGroups(sender));
				} catch (CommandException e) {
					
				}
				
				break;
			case "private":
				if (isOwner) {
					return getListOfStringsMatchingLastWord(args, "true", "false");
				}
				
				break;
			case "members":
				if (isOwner) {
					return getListOfStringsMatchingLastWord(args, "clear", "add", "remove", "list");
				}
				
				break;
			}
			
			break;
		case 3:
			if (isOwner && args[0].equals("members")) {
				switch (args[1]) {
				case "add":
					return getListOfStringsMatchingLastWord(args, minecraftServer.getOnlinePlayerNames());
				case "remove":
					try {
						return getListOfStringsMatchingLastWord(args, listMembers(sender).keySet());
					} catch (CommandException e) {
						
					}
					
					break;
				}
			}
			
			break;
		}
		
		return Collections.emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!isMultimeterClient(sender)) {
			throw new CommandNotFoundException();
		}
		
		if (args.length > 0) {
			switch (args[0]) {
			case "list":
				if (args.length == 1) {
					list(sender);
					return;
				}
				
				throw new WrongUsageException(USAGE_LIST);
			case "subscribe":
				if (args.length == 1) {
					subscribe(sender, null);
					return;
				}
				
				String name = "";
				
				for (int index = 1; index < args.length; index++) {
					name += args[index] + " ";
				}
				
				subscribe(sender, name);
				return;
			case "unsubscribe":
				if (args.length == 1) {
					unsubscribe(sender);
					return;
				}
				
				throw new WrongUsageException(USAGE_UNSUBSCRIBE);
			case "private":
				if (!isOwnerOfSubscription(sender)) {
					break;
				}
				
				switch (args.length) {
				case 1:
					queryPrivate(sender);
					return;
				case 2:
					switch (args[1]) {
					case "true":
						setPrivate(sender, true);
						return;
					case "false":
						setPrivate(sender, false);
						return;
					}
					
					throw new WrongUsageException(USAGE_PRIVATE_SET);
				}
				
				throw new WrongUsageException(USAGE_PRIVATE);
			case "members":
				if (!isOwnerOfSubscription(sender)) {
					break;
				}
				
				if (args.length > 1) {
					switch (args[1]) {
					case "list":
						if (args.length == 2) {
							membersList(sender);
							return;
						}
						
						throw new WrongUsageException(USAGE_MEMBERS_LIST);
					case "add":
						if (args.length == 3) {
							membersAdd(sender, getPlayers(server, sender, args[2]));
							return;
						}
						
						throw new WrongUsageException(USAGE_MEMBERS_ADD);
					case "remove":
						if (args.length == 3) {
							membersRemove(sender, args[2]);
							return;
						}
						
						throw new WrongUsageException(USAGE_MEMBERS_REMOVE);
					case "clear":
						if (args.length == 2) {
							membersClear(sender);
							return;
						}
						
						throw new WrongUsageException(USAGE_MEMBERS_CLEAR);
					}
				}
				
				throw new WrongUsageException(USAGE_MEMBERS);
			case "clear":
				if (args.length == 1) {
					clear(sender);
					return;
				}
				
				throw new WrongUsageException(USAGE_CLEAR);
			}
		}
		
		throw new WrongUsageException(getUsage(sender));
	}
	
	private boolean isMultimeterClient(ICommandSender sender) throws CommandException {
		return execute(sender, player -> multimeter.getMultimeterServer().isMultimeterClient(player));
	}
	
	private boolean isOwnerOfSubscription(ICommandSender sender) throws CommandException {
		return execute(sender, player -> multimeter.isOwnerOfSubscription(player));
	}
	
	private Collection<String> listMeterGroups(ICommandSender sender) throws CommandException {
		List<String> names = new ArrayList<>();
		
		command(sender, player -> {
			for (ServerMeterGroup meterGroup : multimeter.getMeterGroups()) {
				if (!meterGroup.isPrivate() || meterGroup.hasMember(player) || meterGroup.isOwnedBy(player)) {
					names.add(meterGroup.getName());
				}
			}
		});
		
		return names;
	}
	
	private Map<String, UUID> listMembers(ICommandSender sender) throws CommandException {
		Map<String, UUID> names = new HashMap<>();
		
		command(sender, player -> {
			ServerMeterGroup meterGroup = multimeter.getSubscription(player);
			
			if (meterGroup != null && meterGroup.isOwnedBy(player)) {
				for (UUID playerUUID : meterGroup.getMembers()) {
					String playerName = multimeter.getMultimeterServer().getPlayerName(playerUUID);
					
					if (playerName != null) {
						names.put(playerName, playerUUID);
					}
				}
			}
		});
		
		return names;
	}
	
	private void list(ICommandSender sender) throws CommandException {
		Collection<String> names = listMeterGroups(sender);
		
		if (names.isEmpty()) {
			sender.sendMessage(new TextComponentString("There are no meter groups yet!"));
		} else {
			String message = "Meter groups:\n  " + String.join("\n  ", names);
			sender.sendMessage(new TextComponentString(message));
		}
	}
	
	private void subscribe(ICommandSender sender, String name) throws CommandException {
		command(sender, player -> {
			if (name == null) {
				multimeter.subscribeToDefaultMeterGroup(player);
				sender.sendMessage(new TextComponentString("Subscribed to default meter group"));
			} else if (multimeter.hasMeterGroup(name)) {
				ServerMeterGroup meterGroup = multimeter.getMeterGroup(name);
				
				if (!meterGroup.isPrivate() || meterGroup.hasMember(player) || meterGroup.isOwnedBy(player)) {
					multimeter.subscribeToMeterGroup(meterGroup, player);
					sender.sendMessage(new TextComponentString(String.format("Subscribed to meter group \'%s\'", name)));
				} else {
					sender.sendMessage(new TextComponentString("That meter group is private!"));
				}
			} else {
				if (MeterGroup.isValidName(name)) {
					multimeter.createMeterGroup(player, name);
					sender.sendMessage(new TextComponentString(String.format("Created meter group \'%s\'", name)));
				} else {
					sender.sendMessage(new TextComponentString(String.format("\'%s\' is not a valid meter group name!", name)));
				}
			}
		});
	}
	
	private void unsubscribe(ICommandSender sender) throws CommandException {
		command(sender, (meterGroup, player) -> {
			multimeter.unsubscribeFromMeterGroup(meterGroup, player);
			sender.sendMessage(new TextComponentString(String.format("Unsubscribed from meter group \'%s\'", meterGroup.getName())));
		});
	}
	
	private void queryPrivate(ICommandSender sender) throws CommandException {
		command(sender, (meterGroup, player) -> {
			String status = meterGroup.isPrivate() ? "private" : "public";
			sender.sendMessage(new TextComponentString(String.format("Meter group \'%s\' is %s", meterGroup.getName(), status)));
		});
	}
	
	private void setPrivate(ICommandSender sender, boolean isPrivate) throws CommandException {
		command(sender, (meterGroup, player) -> {
			if (meterGroup.isOwnedBy(player)) {
				meterGroup.setPrivate(isPrivate);
				sender.sendMessage(new TextComponentString(String.format("Meter group \'%s\' is now %s", meterGroup.getName(), (isPrivate ? "private" : "public"))));
			} else {
				sender.sendMessage(new TextComponentString("Only the owner of a meter group can change its privacy!"));
			}
		});
	}
	
	private void membersList(ICommandSender sender) throws CommandException {
		Map<String, UUID> members = listMembers(sender);
		
		commandMembers(sender, (meterGroup, owner) -> {
			if (members.isEmpty()) {
				sender.sendMessage(new TextComponentString(String.format("Meter group \'%s\' has no members yet!", meterGroup.getName())));
			} else {
				String message = String.format("Members of meter group \'%s\':\n  ", meterGroup.getName()) + String.join("\n  ", members.keySet());
				sender.sendMessage(new TextComponentString(message));
			}
		});
	}
	
	private void membersAdd(ICommandSender sender, Collection<EntityPlayerMP> players) throws CommandException {
		commandMembers(sender, (meterGroup, owner) -> {
			for (EntityPlayerMP player : players) {
				if (player == owner) {
					sender.sendMessage(new TextComponentString("You cannot add yourself as a member!"));
				} else if (meterGroup.hasMember(player)) {
					sender.sendMessage(new TextComponentString(String.format("Player \'%s\' is already a member of meter group \'%s\'!", player.getName(), meterGroup.getName())));
				} else if (!multimeter.getMultimeterServer().isMultimeterClient(player)) {
					sender.sendMessage(new TextComponentString(String.format("You cannot add player \'%s\' as a member; they do not have %s installed!", player.getName(), RedstoneMultimeter.MOD_NAME)));
				} else {
					multimeter.addMemberToMeterGroup(meterGroup, player.getUniqueID());
					sender.sendMessage(new TextComponentString(String.format("Player \'%s\' is now a member of meter group \'%s\'", player.getName(), meterGroup.getName())));
				}
			}
		});
	}
	
	private void membersRemove(ICommandSender sender, String playerName) throws CommandException {
		commandMembers(sender, (meterGroup, owner) -> {
			Entry<String, UUID> member = findMember(listMembers(sender), playerName);
			
			if (member == null) {
				EntityPlayerMP player = multimeter.getMultimeterServer().getPlayer(playerName);
				
				if (player == owner) {
					sender.sendMessage(new TextComponentString("You cannot remove yourself as a member!"));
				} else {
					sender.sendMessage(new TextComponentString(String.format("Meter group \'%s\' has no member with the name \'%s\'!", meterGroup.getName(), playerName)));
				}
			} else {
				multimeter.removeMemberFromMeterGroup(meterGroup, member.getValue());
				sender.sendMessage(new TextComponentString(String.format("Player \'%s\' is no longer a member of meter group \'%s\'", member.getKey(), meterGroup.getName())));
			}
		});
	}
	
	private Entry<String, UUID> findMember(Map<String, UUID> members, String playerName) {
		String key = playerName.toLowerCase();
		
		for (Entry<String, UUID> member : members.entrySet()) {
			if (member.getKey().toLowerCase().equals(key)) {
				return member;
			}
		}
		
		return null;
	}
	
	private void membersClear(ICommandSender sender) throws CommandException {
		commandMembers(sender, (meterGroup, owner) -> {
			multimeter.clearMembersOfMeterGroup(meterGroup);
			sender.sendMessage(new TextComponentString(String.format("Removed all members from meter group \'%s\'", meterGroup.getName())));
		});
	}
	
	private void commandMembers(ICommandSender sender, MeterGroupCommandExecutor command) throws CommandException {
		command(sender, (meterGroup, player) -> {
			if (meterGroup.isOwnedBy(player)) {
				command.execute(meterGroup, player);
				
				if (!meterGroup.isPrivate()) {
					sender.sendMessage(new TextComponentString("NOTE: this meter group is public; adding/removing members will not have any effect until you make it private!"));
				}
			}
		});
	}
	
	private void clear(ICommandSender sender) throws CommandException {
		command(sender, (meterGroup, player) -> {
			multimeter.clearMeterGroup(meterGroup);
			sender.sendMessage(new TextComponentString(String.format("Removed all meters in meter group \'%s\'", meterGroup.getName())));
		});
	}
	
	private void command(ICommandSender sender, MeterGroupCommandExecutor command) throws CommandException {
		command(sender, player -> {
			ServerMeterGroup meterGroup = multimeter.getSubscription(player);
			
			if (meterGroup == null) {
				sender.sendMessage(new TextComponentString("Please subscribe to a meter group first!"));
			} else {
				command.execute(meterGroup, player);
			}
		});
	}
	
	private void command(ICommandSender sender, MultimeterCommandExecutor command) throws CommandException {
		execute(sender, player -> { command.execute(player); return true; });
	}
	
	private boolean execute(ICommandSender sender, CommandExecutor command) throws CommandException {
		return command.execute(getCommandSenderAsPlayer(sender));
	}
	
	@FunctionalInterface
	private interface MultimeterCommandExecutor {
		
		public void execute(EntityPlayerMP player) throws CommandException;
		
	}
	
	@FunctionalInterface
	private interface MeterGroupCommandExecutor {
		
		public void execute(ServerMeterGroup meterGroup, EntityPlayerMP player) throws CommandException;
		
	}
	
	@FunctionalInterface
	private interface CommandExecutor {
		
		public boolean execute(EntityPlayerMP player) throws CommandException;
		
	}
}
