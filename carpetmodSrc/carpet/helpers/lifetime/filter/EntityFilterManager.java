package carpet.helpers.lifetime.filter;

import carpet.helpers.lifetime.LifeTimeTracker;
import carpet.helpers.lifetime.utils.LifeTimeTrackerUtil;
import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Predicate;

public class EntityFilterManager
{
	private static final EntityFilterManager INSTANCE = new EntityFilterManager();
	private static final Predicate<Entity> DEFAULT_FILTER = entity -> true;

	// key null is for global filter
	private final Map<Class<? extends Entity>, Predicate<Entity>> entityFilter = Maps.newLinkedHashMap();

	public static EntityFilterManager getInstance()
	{
		return INSTANCE;
	}

	public Predicate<Entity> getFilter(@Nullable Class<? extends Entity> entityType)
	{
		return this.entityFilter.getOrDefault(entityType, DEFAULT_FILTER);
	}

	/**
	 * Test right before recording entity spawning
	 * So the entity should be fully initialized
	 */
	public boolean test(Entity entity)
	{
		// global filter, then specific filter
		return this.getFilter(null).test(entity) && this.getFilter(entity.getClass()).test(entity);
	}

	public void setEntityFilter(ICommandSender source, @Nullable Class<? extends Entity> entityType, @Nullable String selectorToken) throws CommandException
	{
		String typeName = getEntityTypeText(entityType).getUnformattedText();
		if (selectorToken != null)
		{
			if (!selectorToken.startsWith("@e"))
			{
				Messenger.m(source, Messenger.s(null, "Unsupported entity filter"));
				Messenger.m(source, Messenger.s( null, "Please enter an @e style entity selector"));
			}
			else
			{
				EntityFilter entityFilter = new EntityFilter(source, selectorToken);
				this.entityFilter.put(entityType, entityFilter);
				Messenger.m(source, String.format("w Entity filter of %s is set to ", typeName), entityFilter.toText());
			}
		}
		else
		{
			this.entityFilter.remove(entityType);
			Messenger.m(source, String.format("w Entity filter of %s removed", typeName));
		}
	}

	public ITextComponent getEntityFilterText(@Nullable Class<? extends Entity> entityType)
	{
		Predicate<Entity> entityPredicate = this.getFilter(entityType);
		return entityPredicate instanceof EntityFilter ? ((EntityFilter)entityPredicate).toText() : Messenger.s(null, "None");
	}

	public ITextComponent getEntityTypeText(@Nullable Class<? extends Entity> entityType)
	{
		return Messenger.s(null, entityType != null ? LifeTimeTrackerUtil.getEntityTypeDescriptor(entityType) : "global");
	}

	public void displayFilter(ICommandSender source, @Nullable Class<? extends Entity> entityType)
	{
		Messenger.m(source, "w Entity filter of ", this.getEntityTypeText(entityType), "w  is ", this.getEntityFilterText(entityType));
	}

	public int displayAllFilters(ICommandSender source)
	{
		Messenger.m(source, Messenger.s(null, String.format("There are %d activated filters", this.entityFilter.size())));
		this.entityFilter.keySet().forEach(entityType -> Messenger.m(
				source,
				Messenger.c(
						"f - ",
						this.getEntityTypeText(entityType),
						"g : ",
						this.getEntityFilterText(entityType),
						"w  ",
						TextUtil.getFancyText(
								null,
								Messenger.s(null, "[×]", "r"),
								Messenger.s(null, "Click to clear filter"),
								new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(
										"/%s filter %s clear",
										LifeTimeTracker.getInstance().getCommandPrefix(),
										entityType != null ? LifeTimeTrackerUtil.getEntityTypeDescriptor(entityType) : "global"
								))
						)
				)
		));
		return 1;
	}
}
