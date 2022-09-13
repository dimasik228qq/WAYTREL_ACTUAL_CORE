/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.autoplay.ExAutoPlayDoMacro;

/**
 * @author Mobius
 */
public class AutoPlayTaskManager implements Runnable
{
	private static final Set<Player> PLAYERS = ConcurrentHashMap.newKeySet();
	private static final Integer AUTO_ATTACK_ACTION = 2;
	private static boolean _working = false;
	
	protected AutoPlayTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 150, 150);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		PLAY: for (Player player : PLAYERS)
		{
			if (!player.isOnline() || player.isInOfflineMode() || !Config.ENABLE_AUTO_PLAY)
			{
				stopAutoPlay(player);
				continue PLAY;
			}
			
			if (player.isCastingNow() || (player.getQueuedSkill() != null))
			{
				continue PLAY;
			}
			
			// Skip thinking.
			final WorldObject target = player.getTarget();
			if ((target != null) && target.isMonster())
			{
				final Monster monster = (Monster) target;
				if (monster.isAlikeDead())
				{
					player.setTarget(null);
				}
				else if ((monster.getTarget() == player) || (monster.getTarget() == null))
				{
					// We take granted that mage classes do not auto hit.
					if (isMageCaster(player))
					{
						continue PLAY;
					}
					
					// Attack and add aggro to the monster.
					if (player.hasAI() && !player.isAttackingNow() && !player.isCastingNow() && !player.isMoving() && !player.isDisabled())
					{
						if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, monster);
						}
						else
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, monster);
						}
						
						// Make sure monster is engaged.
						if (monster.hasAI() && !monster.getAI().isAutoAttacking())
						{
							((Attackable) monster).addDamageHate(player, 0, 100);
						}
					}
					continue PLAY;
				}
			}
			
			// Pickup.
			if (player.getAutoPlaySettings().doPickup())
			{
				PICKUP: for (Item droppedItem : World.getInstance().getVisibleObjectsInRange(player, Item.class, 200))
				{
					// Check if item is reachable.
					if ((droppedItem == null) //
						|| (!droppedItem.isSpawned()) //
						|| !GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), droppedItem.getX(), droppedItem.getY(), droppedItem.getZ(), player.getInstanceWorld()))
					{
						continue PICKUP;
					}
					
					// Move to item.
					if (player.calculateDistance2D(droppedItem) > 70)
					{
						if (!player.isMoving())
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, droppedItem);
						}
						continue PLAY;
					}
					
					// Try to pick it up.
					if (!droppedItem.isProtected() || (droppedItem.getOwnerId() == player.getObjectId()))
					{
						player.doPickupItem(droppedItem);
						continue PLAY; // Avoid pickup being skipped.
					}
				}
			}
			
			// Find target.
			Creature creature = null;
			double closestDistance = Double.MAX_VALUE;
			switch (player.getAutoPlaySettings().getNextTargetMode())
			{
				case 1: // monster
				{
					TARGET: for (Monster nearby : World.getInstance().getVisibleObjectsInRange(player, Monster.class, player.getAutoPlaySettings().isShortRange() ? 600 : 1400))
					{
						// Skip unavailable monsters.
						if ((nearby == null) || nearby.isAlikeDead() || !(nearby instanceof Monster) || (nearby.isPlayable()))
						{
							continue TARGET;
						}
						// Check monster target.
						if (player.getAutoPlaySettings().isRespectfulHunting() && (nearby.getTarget() != null) && (nearby.getTarget() != player) && !player.getServitors().containsKey(nearby.getTarget().getObjectId()))
						{
							continue TARGET;
						}
						// Check if monster is reachable.
						if (nearby.isAutoAttackable(player) //
							&& GeoEngine.getInstance().canSeeTarget(player, nearby)//
							&& GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
						{
							final double monsterDistance = player.calculateDistance2D(nearby);
							if (monsterDistance < closestDistance)
							{
								creature = nearby;
								closestDistance = monsterDistance;
							}
						}
					}
					break;
				}
				case 2: // PC
				{
					TARGET: for (Playable nearby : World.getInstance().getVisibleObjectsInRange(player, Playable.class, player.getAutoPlaySettings().isShortRange() ? 600 : 1400))
					{
						// Skip unavailable PCs.
						if ((nearby == null) || nearby.isAlikeDead() || !(nearby instanceof Playable) || (nearby.isMonster() || (nearby.isNpc())))
						{
							continue TARGET;
						}
						// Check PCs target.
						if (player.getAutoPlaySettings().isRespectfulHunting() && (nearby.getTarget() != null) && (nearby.getTarget() != player) && !player.getServitors().containsKey(nearby.getTarget().getObjectId()))
						{
							continue TARGET;
						}
						// Check if PC is reachable.
						if (GeoEngine.getInstance().canSeeTarget(player, nearby)//
							&& GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
						{
							final double monsterDistance = player.calculateDistance2D(nearby);
							if (monsterDistance < closestDistance)
							{
								creature = nearby;
								closestDistance = monsterDistance;
							}
						}
					}
					break;
				}
				case 3: // Npc
				{
					TARGET: for (Npc nearby : World.getInstance().getVisibleObjectsInRange(player, Npc.class, player.getAutoPlaySettings().isShortRange() ? 600 : 1400))
					{
						// Skip unavailable npcs.
						if ((nearby == null) || nearby.isAlikeDead() || !(nearby instanceof Npc) || (nearby.isMonster()) || (nearby.isPlayable()))
						{
							continue TARGET;
						}
						// Check npc target.
						if (player.getAutoPlaySettings().isRespectfulHunting() && (nearby.getTarget() != null) && (nearby.getTarget() != player) && !player.getServitors().containsKey(nearby.getTarget().getObjectId()))
						{
							continue TARGET;
						}
						// Check if npc is reachable.
						if (GeoEngine.getInstance().canSeeTarget(player, nearby)//
							&& GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
						{
							final double monsterDistance = player.calculateDistance2D(nearby);
							if (monsterDistance < closestDistance)
							{
								creature = nearby;
								closestDistance = monsterDistance;
							}
						}
					}
					break;
				}
				default: // any
				{
					TARGET: for (Creature nearby : World.getInstance().getVisibleObjectsInRange(player, Creature.class, player.getAutoPlaySettings().isShortRange() ? 600 : 1400))
					{
						// Skip unavailable targets.
						if ((nearby == null) || nearby.isAlikeDead())
						{
							continue TARGET;
						}
						// Check target.
						if (player.getAutoPlaySettings().isRespectfulHunting() && (nearby.getTarget() != null) && (nearby.getTarget() != player) && !player.getServitors().containsKey(nearby.getTarget().getObjectId()))
						{
							continue TARGET;
						}
						// Check if target is reachable.
						if (GeoEngine.getInstance().canSeeTarget(player, nearby)//
							&& GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
						{
							final double monsterDistance = player.calculateDistance2D(nearby);
							if (monsterDistance < closestDistance)
							{
								creature = nearby;
								closestDistance = monsterDistance;
							}
						}
					}
					break;
				}
			}
			
			// New target was assigned.
			if (creature != null)
			{
				player.setTarget(creature);
				
				// We take granted that mage classes do not auto hit.
				if (isMageCaster(player))
				{
					continue PLAY;
				}
				
				player.sendPacket(ExAutoPlayDoMacro.STATIC_PACKET);
			}
		}
		
		_working = false;
	}
	
	public void doAutoPlay(Player player)
	{
		if (!PLAYERS.contains(player))
		{
			player.onActionRequest();
			PLAYERS.add(player);
		}
	}
	
	public void stopAutoPlay(Player player)
	{
		PLAYERS.remove(player);
		
		// Pets must follow their owner.
		if (player.hasServitors())
		{
			for (Summon summon : player.getServitors().values())
			{
				summon.followOwner();
			}
		}
		if (player.hasPet())
		{
			player.getPet().followOwner();
		}
	}
	
	public boolean isAutoPlay(Player player)
	{
		return PLAYERS.contains(player);
	}
	
	private boolean isMageCaster(Player player)
	{
		// On Essence auto attack is enabled via the Auto Attack action.
		if (Config.AUTO_PLAY_ATTACK_ACTION)
		{
			return !player.getAutoUseSettings().getAutoActions().contains(AUTO_ATTACK_ACTION);
		}
		
		// Non Essence like.
		return player.isMageClass() && (player.getRace() != Race.ORC);
	}
	
	public static AutoPlayTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoPlayTaskManager INSTANCE = new AutoPlayTaskManager();
	}
}
