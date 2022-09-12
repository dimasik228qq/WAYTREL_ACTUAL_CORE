package org.l2jmobius.gameserver.network.serverpackets.pvpbook;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.pvpbook.Pvpbook;
import org.l2jmobius.gameserver.model.pvpbook.PvpbookInfo;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author nexvill
 */
public class ExPvpBookShareRevengeList implements IClientOutgoingPacket
{
	private final int locationShowCount;
	private final int teleportCount;
	private final Collection<PvpbookInfo> pvpbookInfos;
	private final Player _player;

	public ExPvpBookShareRevengeList(Player player)
	{
		Pvpbook pvpbook = player.getPvpbook();
		locationShowCount = pvpbook.getLocationShowCount();
		teleportCount = pvpbook.getTeleportCount();
		pvpbookInfos = pvpbook.getInfos(false);
		_player = player;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PVPBOOK_SHARE_REVENGE_LIST.writeId(packet);
		packet.writeC(1); // current page
		packet.writeC(1); // max pages
		packet.writeD(pvpbookInfos.size()); // size
		
		int shareType = 1;
		for (PvpbookInfo pvpbookInfo : pvpbookInfos)
		{
			if (pvpbookInfo.getKilledObjectId() != _player.getObjectId())
			{
				shareType = 2;
			}
			
			packet.writeD(shareType); // share type
			packet.writeD(pvpbookInfo.getDeathTime()); // death time
			packet.writeD(locationShowCount); // nShowKillerCount
			packet.writeD(teleportCount); // nTeleportKillerCount
			packet.writeD(1); // nSharedTeleportKillerCount
			packet.writeD(pvpbookInfo.getKilledObjectId()); // killed user DBID
			packet.writeString(pvpbookInfo.getKilledName()); // killed user name
			packet.writeString(pvpbookInfo.getKilledClanName()); // killed user pledge name
			packet.writeD(pvpbookInfo.getKilledLevel()); // killed user level
			packet.writeD(0); // killed user race
			packet.writeD(pvpbookInfo.getKilledClassId()); // killed user class
			packet.writeD(pvpbookInfo.getKillerObjectId()); // killer id
			packet.writeString(pvpbookInfo.getKillerName()); // killer name
			packet.writeString(pvpbookInfo.getKillerClanName()); // killer clan name
			packet.writeD(pvpbookInfo.getKillerLevel()); // killer level
			packet.writeD(0); // race
			packet.writeD(pvpbookInfo.getKillerClassId()); // class id
			packet.writeD(pvpbookInfo.isOnline() ? 2 : 1); // is online
			packet.writeD(pvpbookInfo.getKarma()); // karma
			packet.writeD(shareType == 2 ? pvpbookInfo.getDeathTime() : 0); // shared time
		}
		
		return true;
	}
}