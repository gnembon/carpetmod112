package carpet.helpers.lifetime.filter;

import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class EntityFilter implements Predicate<Entity>
{
	private final String token;
	private final ICommandSender serverCommandSource;
	private final Predicate<Entity> predicate;
	private final Vec3d anchorPos;

	public EntityFilter(ICommandSender sender, String token) throws CommandException
	{
		this.token = token;
		this.serverCommandSource = sender;

		// reference: net.minecraft.command.EntitySelector.matchEntities
		Matcher matcher = EntitySelector.TOKEN_PATTERN.matcher(token);
		if (!matcher.matches())
		{
			throw new SyntaxErrorException("Invalid entity selector: " + token);
		}
		String s = matcher.group(1);
		Map<String, String> params = EntitySelector.getArgumentMap(matcher.group(2));
		this.anchorPos = EntitySelector.getPosFromArguments(params, sender.getPositionVector());

		List<Predicate<Entity>> allPredicates = Lists.newArrayList();
		allPredicates.addAll(EntitySelector.getTypePredicates(params, s));
		allPredicates.addAll(EntitySelector.getXpLevelPredicates(params));
		allPredicates.addAll(EntitySelector.getGamemodePredicates(params));
		allPredicates.addAll(EntitySelector.getTeamPredicates(params));
		allPredicates.addAll(EntitySelector.getScorePredicates(sender, params));
		allPredicates.addAll(EntitySelector.getNamePredicates(params));
		allPredicates.addAll(EntitySelector.getTagPredicates(params));
		allPredicates.addAll(EntitySelector.getRadiusPredicates(params, this.anchorPos));
		allPredicates.addAll(EntitySelector.getRotationsPredicates(params));

		this.predicate = allPredicates.stream().reduce(x -> true, Predicate::and);
	}

	private Vec3d getAnchorPos()
	{
		return this.anchorPos;
	}

	@Override
	public boolean test(Entity testEntity)
	{
		return testEntity != null && this.predicate.test(testEntity);
	}

	public ITextComponent toText()
	{
		return TextUtil.getFancyText(
				"y",
				Messenger.s(null, this.token),
				Messenger.c(
						"w Dimension: ",
						TextUtil.getDimensionNameText(this.serverCommandSource.getEntityWorld().provider.getDimensionType()),
						String.format("w \nAnchor Pos: %s", this.getAnchorPos())
				),
				new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, this.token)
		);
	}
}
