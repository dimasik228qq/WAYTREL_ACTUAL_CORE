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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ManufactureItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class RecipeShopSellList implements IClientOutgoingPacket
{
	private final Player _buyer;
	private final Player _manufacturer;
	private final double _craftRate;
	private final double _craftCritical;
	
	public RecipeShopSellList(Player buyer, Player manufacturer)
	{
		_buyer = buyer;
		_manufacturer = manufacturer;
		_craftRate = _manufacturer.getStat().getValue(Stat.CRAFT_RATE, 0);
		_craftCritical = _manufacturer.getStat().getValue(Stat.CRAFTING_CRITICAL, 0);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIPE_SHOP_SELL_LIST.writeId(packet);
		packet.writeD(_manufacturer.getObjectId());
		packet.writeD((int) _manufacturer.getCurrentMp()); // Creator's MP
		packet.writeD(_manufacturer.getMaxMp()); // Creator's MP
		packet.writeQ(_buyer.getAdena()); // Buyer Adena
		if (!_manufacturer.hasManufactureShop())
		{
			packet.writeD(0);
		}
		else
		{
			packet.writeD(_manufacturer.getManufactureItems().size());
			for (ManufactureItem temp : _manufacturer.getManufactureItems().values())
			{
				packet.writeD(temp.getRecipeId());
				packet.writeD(0); // CanCreate?
				packet.writeQ(temp.getCost());
				packet.writeF(Math.min(_craftRate, 100.0));
				packet.writeC(_craftCritical > 0 ? 1 : 0);
				packet.writeF(Math.min(_craftCritical, 100.0));
			}
		}
		return true;
	}
}
