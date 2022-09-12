package org.l2jmobius.gameserver.network.clientpackets.pvpbook;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.pvpbook.ExPvpBookShareRevengeList;

/**
 * @author nexvill
 */
public class RequestExPvpBookShareRevengeList implements IClientIncomingPacket
{
	private int _userId;
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_userId = packet.readD();
		return true;
	}

	@Override
	public void run(GameClient client)
	{
		Player activeChar = client.getPlayer();
		if (activeChar == null)
			return;

		activeChar.sendPacket(new ExPvpBookShareRevengeList(activeChar));
	}
}