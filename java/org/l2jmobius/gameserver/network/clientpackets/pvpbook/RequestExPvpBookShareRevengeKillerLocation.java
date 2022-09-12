package org.l2jmobius.gameserver.network.clientpackets.pvpbook;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.pvpbook.PvpbookInfo;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.pvpbook.ExPvpBookShareRevengeKillerLocation;
import org.l2jmobius.gameserver.network.serverpackets.pvpbook.ExPvpBookShareRevengeList;

/**
 * @author nexvill
 */
public class RequestExPvpBookShareRevengeKillerLocation implements IClientIncomingPacket
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
		
		if (activeChar.getPvpbook().getLocationShowCount() <= 0)
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
			activeChar.sendPacket(SystemMessageId.THE_TARGET_IS_NO_ONLINE_YOU_CAN_T_USE_THIS_FUNCTION);
			return;
		}
		
		if (killerPlayer.isInInstance())
		{
			activeChar.sendPacket(SystemMessageId.THE_CHARACTER_IS_IN_A_LOCATION_WHERE_IT_IS_IMPOSSIBLE_TO_USE_THIS_FUNCTION);
			return;
		}
		
		int adena_revenge_price = 0;
		switch (activeChar.getPvpbook().getLocationShowCount())
		{
			case 4:
			{
				adena_revenge_price = 50000;
				break;
			}
			case 3:
			{
				adena_revenge_price = 100000;
				break;
			}
			case 2:
			case 1:
			{
				adena_revenge_price = 200000;
				break;
			}
		}
		
		if (!activeChar.reduceAdena("revenge", adena_revenge_price, activeChar, false))
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION);
			return;
		}
		
		activeChar.getPvpbook().reduceLocationShowCount();
		activeChar.sendPacket(new ExPvpBookShareRevengeList(activeChar));
		activeChar.sendPacket(new ExPvpBookShareRevengeKillerLocation(pvpbookInfo.getKillerName(), killerPlayer.getLocation()));
	}
}