package org.l2jmobius.gameserver.network.serverpackets.pvpbook;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author nexvill
 */
public class ExPvpBookShareRevengeNewRevengeInfo implements IClientOutgoingPacket
{
	private final String killerName, killedName;
	private final int shareType;

	public ExPvpBookShareRevengeNewRevengeInfo(String killedName, String killerName, int shareType)
	{
		this.killedName = killedName;
		this.killerName = killerName;
		this.shareType = shareType;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PVPBOOK_SHARE_REVENGE_NEW_REVENGEINFO.writeId(packet);
		packet.writeD(shareType); // share type
		packet.writeString(killedName);
		packet.writeString(killerName);
		
		return true;
	}
}
