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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.enums.PartyDistributionType;
import org.l2jmobius.gameserver.model.BlockList;
import org.l2jmobius.gameserver.model.ClientSettings;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.request.PartyRequest;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.AskJoinParty;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * sample 29 42 00 00 10 01 00 00 00 format cdd
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinParty implements IClientIncomingPacket
{
	private String _name;
	private int _partyDistributionTypeId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_name = packet.readS();
		_partyDistributionTypeId = packet.readD();
		return true;
	}
	
	private void scheduleDeny(Player player)
	{
		if (player != null)
		{
			if (player.getParty() == null)
			{
				player.sendPacket(SystemMessageId.THE_PARTY_IS_DISBANDED);
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_PLAYER_HAS_DECLINED_TO_JOIN_YOUR_PARTY);
			}
			player.onTransactionResponse();
		}
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player requestor = client.getPlayer();
		if (requestor == null)
		{
			return;
		}
		
		final ClientSettings clientSettings = requestor.getClientSettings();
		if (clientSettings.getPartyContributionType() != _partyDistributionTypeId)
		{
			requestor.getClientSettings().setPartyContributionType(_partyDistributionTypeId);
		}
		
		if (FakePlayerData.getInstance().isTalkable(_name))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_INVITED_TO_THE_PARTY);
			sm.addString(FakePlayerData.getInstance().getProperName(_name));
			requestor.sendPacket(sm);
			if (!requestor.isProcessingRequest())
			{
				ThreadPool.schedule(() -> scheduleDeny(requestor), 10000);
				requestor.blockRequest();
			}
			else
			{
				requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			}
			return;
		}
		
		final Player target = World.getInstance().getPlayer(_name);
		if (target == null)
		{
			requestor.sendPacket(SystemMessageId.SELECT_A_PLAYER_YOU_WANT_TO_INVITE_TO_YOUR_PARTY);
			return;
		}
		
		if ((target.getClient() == null) || target.getClient().isDetached())
		{
			requestor.sendMessage("Player is in offline mode.");
			return;
		}
		
		if (requestor.isPartyBanned())
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_PARTICIPATING_IN_A_PARTY_IS_NOT_ALLOWED);
			requestor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (target.isPartyBanned())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_CANNOT_JOIN_A_PARTY);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			return;
		}
		
		if (requestor.isRegisteredOnEvent())
		{
			requestor.sendMessage("You cannot invite to a party while participating in an event.");
			return;
		}
		
		SystemMessage sm;
		if (target.isInParty())
		{
			sm = new SystemMessage(SystemMessageId.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			return;
		}
		
		if (BlockList.isBlocked(target, requestor))
		{
			sm = new SystemMessage(SystemMessageId.C1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			return;
		}
		
		if (target == requestor)
		{
			requestor.sendPacket(SystemMessageId.THE_TARGET_CANNOT_BE_INVITED);
			return;
		}
		if (checkInviteByIgnoredSettings(target, requestor))
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUESTS_AND_CANNOT_RECEIVE_A_PARTY_REQUEST).addPcName(target));
			target.sendPacket(new SystemMessage(SystemMessageId.PARTY_INVITATION_IS_SET_UP_TO_BE_REJECTED_AT_PREFERENCES_THE_PARTY_INVITATION_OF_C1_IS_AUTOMATICALLY_REJECTED).addPcName(requestor));
			return;
		}
		if (target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
		{
			requestor.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (target.isJailed() || requestor.isJailed())
		{
			requestor.sendMessage("You cannot invite a player while is in Jail.");
			return;
		}
		
		if ((target.isInOlympiadMode() || requestor.isInOlympiadMode()) && ((target.isInOlympiadMode() != requestor.isInOlympiadMode()) || (target.getOlympiadGameId() != requestor.getOlympiadGameId()) || (target.getOlympiadSide() != requestor.getOlympiadSide())))
		{
			requestor.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
			return;
		}
		
		sm = new SystemMessage(SystemMessageId.C1_HAS_BEEN_INVITED_TO_THE_PARTY);
		sm.addString(target.getName());
		requestor.sendPacket(sm);
		
		if (!requestor.isInParty())
		{
			createNewParty(target, requestor);
		}
		else
		{
			addTargetToParty(target, requestor);
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(Player target, Player requestor)
	{
		final Party party = requestor.getParty();
		
		// summary of ppl already in party and ppl that get invitation
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
		}
		else if (party.getMemberCount() >= Config.ALT_PARTY_MAX_MEMBERS)
		{
			requestor.sendPacket(SystemMessageId.THE_PARTY_IS_FULL);
		}
		else if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
		}
		else if (!target.hasRequest(PartyRequest.class))
		{
			final PartyRequest request = new PartyRequest(requestor, target, party);
			request.scheduleTimeout(30 * 1000);
			requestor.addRequest(request);
			target.addRequest(request);
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getDistributionType()));
			party.setPendingInvitation(true);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(Player target, Player requestor)
	{
		final PartyDistributionType partyDistributionType = PartyDistributionType.findById(_partyDistributionTypeId);
		if (partyDistributionType == null)
		{
			return;
		}
		
		if (!target.hasRequest(PartyRequest.class))
		{
			final Party party = new Party(requestor, partyDistributionType);
			party.setPendingInvitation(true);
			final PartyRequest request = new PartyRequest(requestor, target, party);
			request.scheduleTimeout(30 * 1000);
			requestor.addRequest(request);
			target.addRequest(request);
			target.sendPacket(new AskJoinParty(requestor.getName(), partyDistributionType));
		}
		else
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
		}
	}
	
	private boolean checkInviteByIgnoredSettings(Player target, Player requestor)
	{
		ClientSettings targetClientSettings = target.getClientSettings();
		boolean condition = targetClientSettings.isPartyRequestRestrictedFromOthers();
		boolean clanCheck = (target.getClan() != null) && (requestor.getClan() != null) && (target.getClan() == requestor.getClan());
		if (condition && ((!targetClientSettings.isPartyRequestRestrictedFromFriends() && target.getFriendList().contains(requestor.getObjectId())) || (!targetClientSettings.isPartyRequestRestrictedFromClan() && clanCheck)))
		{
			condition = false;
		}
		return condition;
	}
}
