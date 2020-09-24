package carpet.utils;

import carpet.mixin.accessors.EntityItemAccessor;
import carpet.mixin.accessors.EntitySlimeAccessor;
import carpet.mixin.accessors.EntityVillagerAccessor;
import carpet.mixin.accessors.EntityZombieVillagerAccessor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityInfo
{
    private static String makeTime(long ticks)
	{
		long secs = ticks/20;
		if (secs < 60)
		{
			return String.format("%d\"", secs);
		}
		if (secs < 60*60)
		{
			return String.format("%d'%d\"", secs/60, secs%60);
		}

		return String.format("%dh%d'%d\"", secs/60/60, (secs % (60*60))/60,(secs % (60*60))%60 );
	}

    private static String display_item(ItemStack item)
	{
		if (item == null)
		{
			return null;
		}
		if (item.isEmpty()) // func_190926_b()
		{
			return null;
		} // func_190916_E()
		String stackname = item.getCount()>1?String.format("%dx%s",item.getCount(), item.getDisplayName()):item.getDisplayName();
		if (item.isItemDamaged())
		{
			stackname += String.format(" %d/%d", item.getMaxDamage()-item.getItemDamage(), item.getMaxDamage());
		}
		if (item.isItemEnchanted())
		{
			stackname += " ( ";
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(item);
			for (Enchantment e: enchants.keySet())
			{
				int level = enchants.get(e);
				String enstring = e.getTranslatedName(level);
				stackname += enstring;
				stackname += " ";
			}
			stackname += ")";
		}
		return stackname;
	}

    public static String entity_short_string(Entity e)
	{
		if (e == null)
		{
			return "None";
		}
		return String.format("%s at [%.1f, %.1f, %.1f]",e.getDisplayName().getUnformattedText(), e.posX, e.posY, e.posZ);
	}

    private static double get_speed(double internal)
	{
		return 43.1*internal;
	}

    private static double get_horse_speed_percent(double internal)
	{
		double min = 0.45*0.25;
		double max = (0.45+0.9)*0.25;
		return 100*(internal-min)/(max-min);
	}

    private static double get_horse_jump(double x)
	{
		 return -0.1817584952 * x*x*x + 3.689713992 * x*x + 2.128599134 * x - 0.343930367;
	}

    private static double get_horse_jump_percent(double internal)
	{
		double min = 0.4;
		double max = 1.0;
		return 100*(internal-min)/(max-min);
	}

    public static List<String> entityInfo(Entity e, World ws)
    {
        List<String> lst = new ArrayList<String>();
		World world = e.getEntityWorld();
        lst.add(entity_short_string(e));
        if (e.isRiding()) { lst.add(String.format(" - Rides: %s", e.getRidingEntity().getDisplayName().getUnformattedText())); }
        if (e.isBeingRidden())
        {
            List<Entity> passengers = e.getPassengers();
            if (passengers.size() == 1)
            {
                lst.add(String.format(" - Is being ridden by: %s", passengers.get(0).getDisplayName().getUnformattedText()));
            }
            else
            {
                lst.add(" - Is being ridden by:");
                for (Entity ei: passengers)
                {
                    lst.add(String.format("   * %s", ei.getDisplayName().getUnformattedText()));
                }
            }
        }
        lst.add(String.format(" - Height: %.2f, Width: %.2f, Eye height: %.2f",e.height, e.width, e.getEyeHeight()));
        lst.add(String.format(" - Age: %s", makeTime(e.ticksExisted)));
		if (ws.provider.getDimensionType().getId() != e.dimension)
		{
			lst.add(String.format(" - Dimension: %s", (e.dimension>0)?"The End":((e.dimension<0)?"Nether":"Overworld")));
		}
        if (e.getFire() > 0) { lst.add(String.format(" - Fire for %d ticks",e.getFire())); }
		if (e.isImmuneToFire() ) { lst.add(" - Immune to fire"); }
		if (e.timeUntilPortal > 0) { lst.add(String.format(" - Portal cooldown for %d ticks",e.timeUntilPortal)); }
		if (e.getIsInvulnerable()) { lst.add(" - Invulnerable"); } //  func_190530_aW()
		if (e.isImmuneToExplosions()) { lst.add(" - Immune to explosions"); }

		if (e instanceof EntityItem)
        {
			EntityItem ei = (EntityItem)e;
			ItemStack stack = ei.getItem();// getEntityItem();
			String stackname = stack.getCount()>1?String.format("%dx%s",stack.getCount(), stack.getDisplayName()):stack.getDisplayName();
			lst.add(String.format(" - Content: %s", stackname));
			lst.add(String.format(" - Despawn Timer: %s", makeTime(((EntityItemAccessor) ei).getAge())));
        }
		if (e instanceof EntityXPOrb)
        {
			EntityXPOrb exp = (EntityXPOrb)e;
            lst.add(String.format(" - Despawn Timer: %s", makeTime(exp.xpOrbAge)));
			lst.add(String.format(" - Xp Value: %s", exp.getXpValue()));
        }
		if (e instanceof EntityItemFrame)
        {
			EntityItemFrame eif = (EntityItemFrame)e;
            lst.add(String.format(" - Content: %s", eif.getDisplayedItem().getDisplayName()));
			lst.add(String.format(" - Rotation: %d", eif.getRotation()));
        }
		if (e instanceof EntityPainting)
        {
			EntityPainting ep = (EntityPainting)e;
            lst.add(String.format(" - Art: %s", ep.art.title ));
        }




        if (e instanceof EntityLivingBase)
        {
            EntityLivingBase elb = (EntityLivingBase)e;
			lst.add(String.format(" - Despawn timer: %s", makeTime(elb.getIdleTime())));

            lst.add(String.format(" - Health: %.2f/%.2f", elb.getHealth(), elb.getMaxHealth()));
			if (elb.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue() > 0.0)
			{
				lst.add(String.format(" - Armour: %.1f",elb.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue()));
			}
			if (elb.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue() > 0.0)
			{
				lst.add(String.format(" - Toughness: %.1f",elb.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
			}
			//lst.add(String.format(" - Base speed: %.1fb/s",get_speed(elb.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue())));



			Collection<PotionEffect> potions = elb.getActivePotionEffects();
			if (!potions.isEmpty())
			{
				lst.add(" - Potion effects:");
				for (PotionEffect pe : potions)
				{
					lst.add(String.format("   * %s%s %s",
							pe.getEffectName().substring(7),
							(pe.getAmplifier()>1)?String.format("x%d",pe.getAmplifier()):"",
							makeTime(pe.getDuration())));
				}
			}
			ItemStack mainhand = elb.getHeldItemMainhand();
			if (!(mainhand.isEmpty()))
			{
				lst.add(String.format(" - Main hand: %s", display_item(mainhand)));
			}
			ItemStack offhand = elb.getHeldItemOffhand();
			if (!(offhand.isEmpty()))
			{
				lst.add(String.format(" - Off hand: %s", display_item(offhand)));
			}
			String armour = "";
			for (ItemStack armourpiece: elb.getArmorInventoryList())
			{
				if (!(armourpiece.isEmpty()))
				{
					armour += String.format("\n   * %s", display_item(armourpiece));
				}
			}
			if (!("".equals(armour)))
			{
				lst.add(String.format(" - Armour:%s", armour));
			}
			if (e instanceof EntityLiving)
            {
				EntityLiving el = (EntityLiving)elb;
				lst.add(String.format(" - Follow range: %.1f",el.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue()));

				lst.add(String.format(" - Movement speed factor: %.2f",el.getMoveHelper().getSpeed()));


				EntityLivingBase target_elb = el.getAttackTarget();
				if (target_elb != null)
				{
					lst.add(String.format(" - Attack target: %s", entity_short_string(target_elb)));
				}
				if (el.canPickUpLoot())
				{
					lst.add(" - Can pick up loot");
				}
				if (el.isNoDespawnRequired())
				{
					lst.add(" - Won't despawn");
				}

				if (e instanceof EntityWither)
				{
					EntityWither ew = (EntityWither)e;
					Entity etarget = world.getEntityByID(ew.getWatchedTargetId(0));
					lst.add(String.format(" - Head 1 target: %s", entity_short_string(etarget) ));
					etarget = world.getEntityByID(ew.getWatchedTargetId(1));
					lst.add(String.format(" - Head 2 target: %s", entity_short_string(etarget) ));
					etarget = world.getEntityByID(ew.getWatchedTargetId(2));
					lst.add(String.format(" - Head 3 target: %s", entity_short_string(etarget) ));
				}
				if (e instanceof EntityCreature)
				{
					EntityCreature ec = (EntityCreature) e;
					if (ec.hasHome())
					{
						BlockPos pos = ec.getHomePosition();
						lst.add(String.format(" - Home position: %d blocks around [%d, %d, %d]", (int)ec.getMaximumHomeDistance(), pos.getX(),pos.getY(),pos.getZ()));
					}
					if (e instanceof EntityAgeable)
					{
						EntityAgeable eage = (EntityAgeable) e;
						if (eage.getGrowingAge() < 0)
						{
							lst.add(String.format(" - Time till adulthood: %s", makeTime(-eage.getGrowingAge())));
						}
						if (eage.getGrowingAge() > 0)
						{
							lst.add(String.format(" - Mating cooldown: %s", makeTime(eage.getGrowingAge())));
						}
						if (e instanceof EntityVillager)
						{
							EntityVillager ev = (EntityVillager) e;

							InventoryBasic vinv = ev.getVillagerInventory();
							String inventory_content = "";
							for (int i = 0; i < vinv.getSizeInventory(); ++i)
							{
								ItemStack vstack = vinv.getStackInSlot(i);
								if (!vstack.isEmpty())
								{
									inventory_content += String.format("\n   * %d: %s", i, display_item(vstack));
								}
							}
							if (!("".equals(inventory_content)))
							{
								lst.add(String.format(" - Inventory:%s", inventory_content));
							}
							if (((EntityVillagerAccessor) ev).getWealth()>0)
							{
								lst.add(String.format(" - Wealth: %d emeralds", ev.getWealth()));
							}
						}
						if (e instanceof AbstractHorse)
						{
							AbstractHorse ah = (AbstractHorse) e;
							lst.add(String.format(" - Horse Speed: %.2f b/s (%.1f%%%%)",
								get_speed(elb.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()),
								get_horse_speed_percent(elb.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue())
								));
							lst.add(String.format(" - Horse Jump: %.2f b/s (%.1f%%%%)",
								get_horse_jump(ah.getHorseJumpStrength()),
								get_horse_jump_percent(ah.getHorseJumpStrength())
								));
						}
					}
					if (e instanceof EntityMob)
					{
						lst.add(String.format(" - Base attack: %.1f",elb.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
						if (e instanceof EntityZombieVillager)
						{
							EntityZombieVillager ezv = (EntityZombieVillager) e;
							int conversionTime = ((EntityZombieVillagerAccessor) ezv).getConversionTime();
							if (conversionTime > 0)
							{
								lst.add(String.format(" - Convert to villager in: %s", makeTime(conversionTime)));
							}
						}
					}
				}
				if (e instanceof EntitySlime)
				{
					lst.add(String.format(" - Base attack: %.1f", (float)((EntitySlimeAccessor)e).invokeGetAttackStrength()));
				}
			}
        }

		return lst;
	}

    static void issue_entity_info(EntityPlayer player)
	{
        try
        {
			player.getServer().getCommandManager().executeCommand(player, "entityinfo @e[r=5,c=5,type=!player]");
        }
        catch (Throwable ignored)
        {
        }
	}
}
