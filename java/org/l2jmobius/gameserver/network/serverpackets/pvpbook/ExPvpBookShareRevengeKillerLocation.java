package org.l2jmobius.gameserver.network.serverpackets.pvpbook;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author nexvill
 */
public class ExPvpBookShareRevengeKillerLocation implements IClientOutgoingPacket
{
	private final String killerName;
	private final Location killerLoc;

	public ExPvpBookShareRevengeKillerLocation(String killerName, Location killerLoc)
	{
		this.killerName = killerName;
		this.killerLoc = killerLoc;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PVPBOOK_SHARE_REVENGE_KILLER_LOCATION.writeId(packet);
		packet.writeString(killerName);
		packet.writeD(killerLoc.getX());
		packet.writeD(killerLoc.getY());
		packet.writeD(killerLoc.getZ());
		
		return true;
	}
}