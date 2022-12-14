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
package org.l2jmobius.gameserver.network.serverpackets.subjugation;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.PurgePlayerHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, Serenitty
 */
public class ExSubjugationSidebar implements IClientOutgoingPacket
{
	private final Player _player;
	private final PurgePlayerHolder _purgeData;
	
	public ExSubjugationSidebar(Player player, PurgePlayerHolder purgeData)
	{
		_player = player;
		_purgeData = purgeData;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SUBJUGATION_SIDEBAR.writeId(packet);
		packet.writeD(_player == null ? 0 : _player.getPurgeLastCategory());
		packet.writeD(_purgeData == null ? 0 : _purgeData.getPoints()); // 1000000 = 100 percent
		packet.writeD(_purgeData == null ? 0 : _purgeData.getKeys());
		packet.writeD(0);
		return true;
	}
}
