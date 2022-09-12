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
package ai.others;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnAttackableKill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * @author NasSeKa
 */
public class LCoinDrop extends AbstractNpcAI
{
	private static final int LEVEL_DIFFERENCE = 15;
	private static final int COIN_ID = 91663; // coin drop ID
	private static final int MAX_DROPS = 86400; // coin max daily drop
	private int count = 10;
	private static final long TIMEOUT = 20000; // in milliseconds
	
	@RegisterEvent(EventType.ON_ATTACKABLE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_MONSTERS)
	public void onAttackableKill(OnAttackableKill event)
	{
		final Creature creature = event.getTarget();
		if ((creature != null) && creature.isMonster())
		{
			final Player player = event.getAttacker().getActingPlayer();
			if ((player != null) && (Math.abs(player.getLevel() - creature.getLevel()) <= LEVEL_DIFFERENCE))
			{
				int currentCount = player.getVariables().getInt(PlayerVariables.DAILY_L_COIN_DROP, 0);
				if (!player.getCoinTimeout() && (currentCount < MAX_DROPS))
				{
					if ((player.getLevel() > 0) && (player.getLevel() < 40))
					{
						return;
					}
					else if ((player.getLevel() >= 40) && (player.getLevel() < 80))
					{
						count = 10;
					}
					else if (player.getLevel() == 81)
					{
						count = 11;
					}
					else if (player.getLevel() == 82)
					{
						count = 12;
					}
					else if (player.getLevel() == 83)
					{
						count = 13;
					}
					else if (player.getLevel() == 84)
					{
						count = 14;
					}
					else if (player.getLevel() == 85)
					{
						count = 15;
					}
					else if (player.getLevel() == 86)
					{
						count = 16;
					}
					else if (player.getLevel() == 87)
					{
						count = 17;
					}
					else if (player.getLevel() == 88)
					{
						count = 18;
					}
					else if (player.getLevel() == 89)
					{
						count = 19;
					}
					else if (player.getLevel() >= 90)
					{
						count = 20;
					}
					if ((currentCount + count) > MAX_DROPS)
					{
						count = MAX_DROPS - currentCount;
					}
					player.addItem("Coin", COIN_ID, count, player, true);
					player.getVariables().set(PlayerVariables.DAILY_L_COIN_DROP, currentCount + count);
					player.setCoinTimeout(true);
					if (player.getVariables().getInt(PlayerVariables.DAILY_L_COIN_DROP, 0) >= MAX_DROPS)
					{
						String s = "You have reached your daily limit for L Coin Drops.";
						player.sendPacket(new ExShowScreenMessage(s, ExShowScreenMessage.TOP_CENTER, 10000));
						player.sendMessage(s);
					}
					ThreadPool.schedule(() ->
					{
						player.setCoinTimeout(false);
					}, TIMEOUT);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new LCoinDrop();
	}
}