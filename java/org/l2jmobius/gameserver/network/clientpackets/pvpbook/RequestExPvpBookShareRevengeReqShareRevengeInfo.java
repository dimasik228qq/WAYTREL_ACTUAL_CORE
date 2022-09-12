package org.l2jmobius.gameserver.network.clientpackets.pvpbook;

import java.util.concurrent.TimeUnit;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.pvpbook.PvpbookInfo;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.pvpbook.ExPvpBookShareRevengeNewRevengeInfo;

/**
 * @author nexvill
 */
public class RequestExPvpBookShareRevengeReqShareRevengeInfo implements IClientIncomingPacket
{
	private String killedName;
	private String killerName;
	private int shareType;

	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		killedName = packet.readString();
		killerName = packet.readString();
		shareType = packet.readD();
		return true;
	}

	@Override
	public void run(GameClient client)
	{
		Player activeChar = client.getPlayer();
		if (activeChar == null)
			return;
		
		if (RankManager.getInstance().getRankList().size() == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_USE_BECAUSE_THERE_IS_NO_RATING_INFO));
			return;
		}
		
		if (!activeChar.destroyItemByItemId("pvpbook", 57, 100_000, activeChar, false))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ADENA));
			return;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED).addInt(100_000));
		}
		
		int objId = 0;
		if (shareType == 2)
		{
			final StatSet ranker = RankManager.getInstance().getRankList().get(1);
			objId = ranker.getInt("charId");
			Player player = World.getInstance().getPlayer(objId);
			if (player != null)
			{
				Player killer = World.getInstance().getPlayer(killerName);
				if (killer != null)
				{
					if (killer.getObjectId() == objId)
					{
						return;
					}
					
					PvpbookInfo pvpbookInfo = player.getPvpbook().addInfo(activeChar, killer, activeChar.getPvpbook().getInfo(killer.getObjectId()).getDeathTime(), (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					if(pvpbookInfo != null)
					{
						player.sendPacket(new ExPvpBookShareRevengeNewRevengeInfo(killedName, killerName, shareType));
					}
				}
				else
				{
					PvpbookInfo info = activeChar.getPvpbook().getInfo(killerName);
					int killerObjId = info.getKillerObjectId();
					
					if (killerObjId == objId)
					{
						return;
					}
					
					int deathTime = info.getDeathTime();
					int killerLevel = info.getKillerLevel();
					int killerClassId = info.getKillerClassId();
					int karma = info.getKarma();
					PvpbookInfo pvpbookInfo = player.getPvpbook().addInfo(activeChar.getObjectId(), killerObjId, deathTime, killedName, killerName, activeChar.getLevel(), killerLevel, activeChar.getClassId().getId(), killerClassId, info.getKilledClanName(), info.getKillerClanName(), karma, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					if(pvpbookInfo != null)
					{
						player.sendPacket(new ExPvpBookShareRevengeNewRevengeInfo(killedName, killerName, shareType));
					}
				}
			}
			else
			{
				PvpbookInfo info = activeChar.getPvpbook().getInfo(killerName);
				int killerObjId = info.getKillerObjectId();
				
				if (killerObjId == objId)
				{
					return;
				}
				
				int deathTime = info.getDeathTime();
				int killerLevel = info.getKillerLevel();
				int killerClassId = info.getKillerClassId();
				int karma = info.getKarma();
				activeChar.getPvpbook().insert(objId, activeChar.getObjectId(), killerObjId, deathTime, killedName, killerName, activeChar.getLevel(), killerLevel, activeChar.getClassId().getId(), killerClassId, info.getKilledClanName(), info.getKillerClanName(), karma, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			}
		}
		else
		{
			for (ClanMember member : activeChar.getClan().getMembers())
			{
				objId = member.getObjectId();
				Player player = World.getInstance().getPlayer(objId);
				if (player != null)
				{
					Player killer = World.getInstance().getPlayer(killerName);
					if (killer != null)
					{
						if (killer.getObjectId() == objId)
						{
							return;
						}
						
						PvpbookInfo pvpbookInfo = player.getPvpbook().addInfo(activeChar, killer, activeChar.getPvpbook().getInfo(killer.getObjectId()).getDeathTime(), (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
						if(pvpbookInfo != null)
						{
							player.sendPacket(new ExPvpBookShareRevengeNewRevengeInfo(killedName, killerName, shareType));
						}
					}
					else
					{
						PvpbookInfo info = activeChar.getPvpbook().getInfo(killerName);
						int killerObjId = info.getKillerObjectId();
						
						if (killerObjId == objId)
						{
							return;
						}
						
						int deathTime = info.getDeathTime();
						int killerLevel = info.getKillerLevel();
						int killerClassId = info.getKillerClassId();
						int karma = info.getKarma();
						PvpbookInfo pvpbookInfo = player.getPvpbook().addInfo(activeChar.getObjectId(), killerObjId, deathTime, killedName, killerName, activeChar.getLevel(), killerLevel, activeChar.getClassId().getId(), killerClassId, info.getKilledClanName(), info.getKillerClanName(), karma, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
						if(pvpbookInfo != null)
						{
							player.sendPacket(new ExPvpBookShareRevengeNewRevengeInfo(killedName, killerName, shareType));
						}
					}
				}
				else
				{
					PvpbookInfo info = activeChar.getPvpbook().getInfo(killerName);
					int killerObjId = info.getKillerObjectId();
					
					if (killerObjId == objId)
					{
						return;
					}
					
					int deathTime = info.getDeathTime();
					int killerLevel = info.getKillerLevel();
					int killerClassId = info.getKillerClassId();
					int karma = info.getKarma();
					activeChar.getPvpbook().insert(objId, activeChar.getObjectId(), killerObjId, deathTime, killedName, killerName, activeChar.getLevel(), killerLevel, activeChar.getClassId().getId(), killerClassId, info.getKilledClanName(), info.getKillerClanName(), karma, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
				}
			}
		}
	}
}