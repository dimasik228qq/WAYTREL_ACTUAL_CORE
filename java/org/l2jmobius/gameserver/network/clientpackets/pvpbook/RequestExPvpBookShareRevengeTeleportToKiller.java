package org.l2jmobius.gameserver.network.clientpackets.pvpbook;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.pvpbook.PvpbookInfo;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author nexvill
 */
public class RequestExPvpBookShareRevengeTeleportToKiller implements IClientIncomingPacket
{
	private String killedName;
	private String killerName;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		killedName = packet.readString();
		killerName = packet.readString();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		Player activeChar = client.getPlayer();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getPvpbook().getTeleportCount() <= 0)
		{
			return;
		}
		
		PvpbookInfo pvpbookInfo = activeChar.getPvpbook().getInfo(killerName);
		if (pvpbookInfo == null)
		{
			return;
		}
		
		Player killerPlayer = pvpbookInfo.getKiller();
		if ((killerPlayer == null) || !killerPlayer.isOnline())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_TARGET_IS_NO_ONLINE_YOU_CAN_T_USE_THIS_FUNCTION));
			return;
		}
		
		if (killerPlayer.isInInstance())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_CHARACTER_IS_IN_A_LOCATION_WHERE_IT_IS_IMPOSSIBLE_TO_USE_THIS_FUNCTION_2));
			return;
		}
		
		int l_coin_revenge_price = 10;
		switch (activeChar.getPvpbook().getTeleportCount())
		{
			case 4:
			{
				l_coin_revenge_price = 50;
				break;
			}
			case 3:
			{
				l_coin_revenge_price = 100;
				break;
			}
			case 2:
			case 1:
			{
				l_coin_revenge_price = 200;
				break;
			}
		}
		
		if (!activeChar.destroyItemByItemId("pvpbook", 91663, l_coin_revenge_price, activeChar, false))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_L2_COINS));
			return;
		}
		
		activeChar.getPvpbook().reduceTeleportCount();
		activeChar.teleToLocation(killerPlayer.getLocation());
	}
}