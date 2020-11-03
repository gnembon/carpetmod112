package carpet.helpers;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.mixin.accessors.EntityAITasksAccessor;
import carpet.mixin.accessors.EntityLivingAccessor;
import carpet.utils.extensions.AccessibleAITaskEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AIHelper {
    private static final WeakHashMap<EntityAITasks, EntityLiving> TASK_TO_ENTITY_MAP = new WeakHashMap<>();
    private static final WeakHashMap<EntityLiving, WeakHashMap<EntityAIBase, Supplier<String>>> DETAILED_INFO = new WeakHashMap<>();
    private static final Map<Class<? extends EntityAIBase>, String> TASK_NAME_MAP = new HashMap<>();

    static {
        TASK_NAME_MAP.put(EntityAIAttackMelee.class, "Melee attack");
        TASK_NAME_MAP.put(EntityAIAttackRanged.class, "Ranged attack");
        TASK_NAME_MAP.put(EntityAIAttackRangedBow.class, "Ranged bow attack");
        TASK_NAME_MAP.put(EntityAIAvoidEntity.class, "Avoiding other entity");
        TASK_NAME_MAP.put(EntityAIBeg.class, "Beg");
        TASK_NAME_MAP.put(EntityAIBreakDoor.class, "Breaking door");
        TASK_NAME_MAP.put(EntityAICreeperSwell.class, "Creeper swelling");
        TASK_NAME_MAP.put(EntityAIDefendVillage.class, "Defending village");
        TASK_NAME_MAP.put(EntityAIDoorInteract.class, "Interacting with door");
        TASK_NAME_MAP.put(EntityAIEatGrass.class, "Eating grass");
        TASK_NAME_MAP.put(EntityAIFindEntityNearest.class, "Looking for nearest other entity");
        TASK_NAME_MAP.put(EntityAIFindEntityNearestPlayer.class, "Looking for nearest player");
        TASK_NAME_MAP.put(EntityAIFleeSun.class, "Seeking shelter from the sun");
        TASK_NAME_MAP.put(EntityAIFollow.class, "Following other entity");
        TASK_NAME_MAP.put(EntityAIFollowGolem.class, "Following golem");
        TASK_NAME_MAP.put(EntityAIFollowOwner.class, "Following owner");
        TASK_NAME_MAP.put(EntityAIFollowOwnerFlying.class, "Following owner while flying");
        TASK_NAME_MAP.put(EntityAIFollowParent.class, "Following parent");
        TASK_NAME_MAP.put(EntityAIHarvestFarmland.class, "Farming");
        TASK_NAME_MAP.put(EntityAIHurtByTarget.class, "Hurt by another entity");
        TASK_NAME_MAP.put(EntityAILandOnOwnersShoulder.class, "Land on owners sholder");
        TASK_NAME_MAP.put(EntityAILeapAtTarget.class, "Leaping at target");
        TASK_NAME_MAP.put(EntityAILlamaFollowCaravan.class, "Llama following caravan");
        TASK_NAME_MAP.put(EntityAILookAtTradePlayer.class, "Looking at player");
        TASK_NAME_MAP.put(EntityAILookAtVillager.class, "Looking at villager");
        TASK_NAME_MAP.put(EntityAILookIdle.class, "Idle, looking around");
        TASK_NAME_MAP.put(EntityAIMate.class, "Mating (Animals)");
        TASK_NAME_MAP.put(EntityAIMoveIndoors.class, "Moving indoors");
        TASK_NAME_MAP.put(EntityAIMoveThroughVillage.class, "Moving through village");
        TASK_NAME_MAP.put(EntityAIMoveToBlock.class, "Moving to block");
        TASK_NAME_MAP.put(EntityAIMoveTowardsRestriction.class, "Moving towards restriction");
        TASK_NAME_MAP.put(EntityAIMoveTowardsTarget.class, "Moving towards target");
        TASK_NAME_MAP.put(EntityAINearestAttackableTarget.class, "Looking for nearest target");
        TASK_NAME_MAP.put(EntityAIOcelotAttack.class, "Ocelot attacking");
        TASK_NAME_MAP.put(EntityAIOcelotSit.class, "Ocelot sitting");
        TASK_NAME_MAP.put(EntityAIOpenDoor.class, "Opening door");
        TASK_NAME_MAP.put(EntityAIOwnerHurtByTarget.class, "Owner hurt by target");
        TASK_NAME_MAP.put(EntityAIOwnerHurtTarget.class, "Owner hurts target");
        TASK_NAME_MAP.put(EntityAIPanic.class, "Panicking");
        TASK_NAME_MAP.put(EntityAIPlay.class, "Playing");
        TASK_NAME_MAP.put(EntityAIRestrictOpenDoor.class, "Prevented from opening door");
        TASK_NAME_MAP.put(EntityAIRestrictSun.class, "Avoiding sun");
        TASK_NAME_MAP.put(EntityAIRunAroundLikeCrazy.class, "Running around like crazy");
        TASK_NAME_MAP.put(EntityAISit.class, "Sitting");
        TASK_NAME_MAP.put(EntityAISkeletonRiders.class, "Riding");
        TASK_NAME_MAP.put(EntityAISwimming.class, "Swimming");
        TASK_NAME_MAP.put(EntityAITarget.class, "Targeting");
        TASK_NAME_MAP.put(EntityAITargetNonTamed.class, "Targeting untamed animal");
        TASK_NAME_MAP.put(EntityAITempt.class, "Tempted by player");
        TASK_NAME_MAP.put(EntityAITradePlayer.class, "Trading with player");
        TASK_NAME_MAP.put(EntityAIVillagerInteract.class, "Interacting with villager");
        TASK_NAME_MAP.put(EntityAIVillagerMate.class, "Mating (Villagers)");
        TASK_NAME_MAP.put(EntityAIWander.class, "Wandering");
        TASK_NAME_MAP.put(EntityAIWanderAvoidWater.class, "Wandering (Land)");
        TASK_NAME_MAP.put(EntityAIWanderAvoidWaterFlying.class, "Wandering (Air)");
        TASK_NAME_MAP.put(EntityAIWatchClosest.class, "Looking at closest entity");
        TASK_NAME_MAP.put(EntityAIZombieAttack.class, "Zombie attacking");
    }

    public static Stream<EntityAIBase> getCurrentTasks(EntityLiving e) {
        return ((EntityAITasksAccessor) getTasks(e)).getExecutingTaskEntries().stream()
                .sorted(Comparator.comparingInt(AccessibleAITaskEntry::getPriority).reversed())
                .map(AccessibleAITaskEntry::getAction);
    }

    public static Stream<String> getCurrentTaskNames(EntityLiving e, Map<EntityAIBase, Supplier<String>> details) {
        return getCurrentTasks(e).map(task -> getTaskName(task, details));
    }

    public static String getTaskName(EntityAIBase task) {
        return getTaskName(task, null);
    }

    public static String getTaskName(EntityAIBase task, Map<EntityAIBase, Supplier<String>> details) {
        String detailsInfo = details != null && details.containsKey(task) ? ": " + details.get(task).get() : "";
        String taskName = "Unknown";
        for (Class<? extends EntityAIBase> cls = task.getClass(); cls != EntityAIBase.class; cls = (Class<? extends EntityAIBase>) cls.getSuperclass()) {
            String name = TASK_NAME_MAP.get(cls);
            if (name != null) {
                taskName = name;
                break;
            }
        }
        return taskName + detailsInfo;
    }

    public static String getInfo(EntityAITasks tasks, EntityAIBase task) {
        return "Entity: " + getName(getOwner(tasks)) + ", Task: " + getTaskName(task);
    }

    public static String getName(@Nullable Entity entity) {
        if (entity == null) return "unknown";
        if (!entity.hasCustomName()) return entity.getName();
        String id = EntityList.getEntityString(entity);
        if (id == null) id = "generic";
        return I18n.translateToLocal("entity." + id + ".name");
    }

    public static Optional<String> formatCurrentTasks(EntityLiving e, Map<EntityAIBase, Supplier<String>> details) {
        return Optional.of(getCurrentTaskNames(e, details).collect(Collectors.joining(",")));
    }

    @Nullable
    public static EntityLiving getOwner(EntityAITasks tasks) {
        return TASK_TO_ENTITY_MAP.computeIfAbsent(tasks, t -> {
            for (WorldServer world : CarpetServer.minecraft_server.worlds) {
                for (EntityLiving e : world.getEntities(EntityLiving.class, x -> true)) {
                    if (((EntityLivingAccessor) e).getTasks() == tasks) return e;
                }
            }
            return null;
        });
    }

    public static void update(EntityAITasks tasks) {
        if (!CarpetSettings.displayMobAI) return;
        EntityLiving owner = getOwner(tasks);
        if (owner == null) return;
        Map<EntityAIBase, Supplier<String>> details = DETAILED_INFO.get(owner);
        Optional<String> formatted = formatCurrentTasks(owner, details);
        if (!formatted.isPresent()) return;
        owner.setCustomNameTag(formatted.get());
    }
    public static void setDetailedInfo(EntityLiving owner, EntityAIBase task, String info) {
        setDetailedInfo(owner, task, () -> info);
    }

    public static void setDetailedInfo(EntityLiving owner, EntityAIBase task, Supplier<String> info) {
        DETAILED_INFO.computeIfAbsent(owner, x -> new WeakHashMap<>()).put(task, info);
        TASK_TO_ENTITY_MAP.put(getTasks(owner), owner);
        update(getTasks(owner));
    }

    public static EntityAITasks getTasks(EntityLiving e) {
        return ((EntityLivingAccessor) e).getTasks();
    }
}
