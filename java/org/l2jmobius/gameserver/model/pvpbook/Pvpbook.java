package org.l2jmobius.gameserver.model.pvpbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.SqlBatch;

public class Pvpbook {
	private static final String RESTORE_SQL_QUERY = "SELECT * FROM character_pvpbook WHERE char_id=?";
	private static final String STORE_SQL_QUERY = "REPLACE INTO character_pvpbook (char_id,killed_id,killer_id,death_time,killed_name,killer_name,killed_level,killer_level,killed_class_id,killer_class_id,killed_clan_name,killer_clan_name,karma, shared_time) VALUES";
	private static final String INSERT_SQL_QUERY = "INSERT IGNORE INTO character_pvpbook (char_id,killed_id,killer_id,death_time,killed_name,killer_name,killed_level,killer_level,killed_class_id,killer_class_id,killed_clan_name,killer_clan_name,karma, shared_time) VALUES";
	private static final String CLEANUP_SQL_QUERY = "DELETE FROM character_pvpbook WHERE char_id=?";
	private static final String DELETE_REVENGED_BY_HELP = "DELETE FROM character_pvpbook WHERE killed_id=? AND killer_id=?";
	private static final String DELETE_EXPIRED_SQL_QUERY = "DELETE FROM character_pvpbook WHERE (? - death_time) > ?";
	
	public static final int EXPIRATION_DELAY = (int) TimeUnit.HOURS.toSeconds(24);

	private static final String LOCATION_SHOW_COUNT_VAR = "pvpbook_loc_show_count";
	private static final String TELEPORT_COUNT_VAR = "pvpbook_teleport_count";
	private static final String TELEPORT_HELP_COUNT_VAR = "pvpbook_teleport_help_count";

	private static final int MAX_LOCATION_SHOW_COUNT_PER_DAY = 5;
	private static final int MAX_TELEPORT_COUNT_PER_DAY = 5;
	private static final int MAX_TELEPORT_HELP_COUNT_PER_DAY = 1;

	public static final long LOCATION_SHOW_PRICE = 0;
	public static final long TELEPORT_PRICE = 10;
	public static final int TELEPORT_HELP_PRICE = 100;

	private final Player owner;
	private final Map<Integer, PvpbookInfo> infos = new HashMap<>();

	public Pvpbook(Player owner) {
		this.owner = owner;
		deleteExpired();
	}

	public Player getOwner() {
		return owner;
	}

	public void restore(Player player) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getConnection();
			statement = con.prepareStatement(RESTORE_SQL_QUERY);
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			Pvpbook pvpbook = player.getPvpbook();
			while (rset.next()) {
				int deathTime = rset.getInt("death_time");
				if (Pvpbook.isExpired(deathTime))
					continue;

				int killedId = rset.getInt("killed_id");
				Player killedPlayer = World.getInstance().getPlayer(killedId);
				
				int killerId = rset.getInt("killer_id");
				Player killerPlayer = World.getInstance().getPlayer(killerId);
				int sharedTime = rset.getInt("shared_time");
				if ((killedPlayer != null) && (killerPlayer != null)) {
					pvpbook.addInfo(killedPlayer, killerPlayer, deathTime, sharedTime);
				} else {
					String killedName = rset.getString("killed_name");
					String killerName = rset.getString("killer_name");
					int killedLevel = rset.getInt("killed_level");
					int killerLevel = rset.getInt("killer_level");
					int killedClassId = rset.getInt("killed_class_id");
					int killerClassId = rset.getInt("killer_class_id");
					String killedClanName = rset.getString("killed_clan_name");
					String killerClanName = rset.getString("killer_clan_name");
					int karma = rset.getInt("karma");
					pvpbook.addInfo(killedId, killerId, deathTime, killedName, killerName, killedLevel, killerLevel, killedClassId, killerClassId, killedClanName, killerClanName, karma, sharedTime);
				}
			}
		} catch (Exception e) {
			return;
		}

	}

	public void store(Player player) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getConnection();
			statement = con.prepareStatement(CLEANUP_SQL_QUERY);
			statement.setInt(1, player.getObjectId());
			statement.execute();
			statement.close();

			SqlBatch b = new SqlBatch(STORE_SQL_QUERY);
			for (PvpbookInfo pvpbookInfo : player.getPvpbook().getInfos(false)) {
				StringBuilder sb = new StringBuilder("(");
				sb.append(player.getObjectId()).append(",");
				sb.append(pvpbookInfo.getKilledObjectId()).append(",");
				sb.append(pvpbookInfo.getKillerObjectId()).append(",");
				sb.append(pvpbookInfo.getDeathTime()).append(",");
				sb.append("'").append(pvpbookInfo.getKilledName()).append("'").append(",");
				sb.append("'").append(pvpbookInfo.getKillerName()).append("'").append(",");
				sb.append(pvpbookInfo.getKilledLevel()).append(",");
				sb.append(pvpbookInfo.getKillerLevel()).append(",");
				sb.append(pvpbookInfo.getKilledClassId()).append(",");
				sb.append(pvpbookInfo.getKillerClassId()).append(",");
				sb.append("'").append(pvpbookInfo.getKilledClanName()).append("'").append(",");
				sb.append("'").append(pvpbookInfo.getKillerClanName()).append("'").append(",");
				sb.append(pvpbookInfo.getKarma()).append(",");
				sb.append(pvpbookInfo.getSharedTime()).append(")");
				b.write(sb.toString());
			}
			if (!b.isEmpty())
				statement.executeUpdate(b.close());
		} catch (Exception e) {
			return;
		}
	}
	
	public boolean insert(int objId, int killedObjId, int killerObjId, int deathTime, String killedName, String killerName, int killedLevel, int killerLevel, int killedClassId, int killerClassId, String killedClanName, String killerClanName, int karma, int sharedTime)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setInt(1, objId);
			statement.setInt(2, killedObjId);
			statement.setInt(3, killerObjId);
			statement.setInt(4, deathTime);
			statement.setString(5, killedName);
			statement.setString(6, killerName);
			statement.setInt(7, killedLevel);
			statement.setInt(8, killerLevel);
			statement.setInt(9, killedClassId);
			statement.setInt(10, killerClassId);
			statement.setString(11, killedClanName);
			statement.setString(12, killerClanName);
			statement.setInt(13, karma);
			statement.setInt(14, sharedTime);
			statement.execute();
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	public void deleteRevengedByHelp(int killedId, int killerId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getConnection();
			statement = con.prepareStatement(DELETE_REVENGED_BY_HELP);
			statement.setInt(1, killedId);
			statement.setInt(2, killedId);
			statement.execute();
		}
		catch (Exception e)
		{
			return;
		}
	}

	private void deleteExpired() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getConnection();
			statement = con.prepareStatement(DELETE_EXPIRED_SQL_QUERY);
			statement.setInt(1, (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			statement.setInt(2, Pvpbook.EXPIRATION_DELAY);
			statement.execute();
		} catch (final Exception e) {
			return;
		}
	}

	public PvpbookInfo addInfo(Player killed, Player killer, int deathTime, int sharedTime) {
		if (!isExpired(deathTime)) {
			PvpbookInfo pvpbookInfo = new PvpbookInfo(this, killed, killer, deathTime, sharedTime);
			infos.put(pvpbookInfo.getKillerObjectId(), pvpbookInfo);
			return pvpbookInfo;
		}
		return null;
	}

	public PvpbookInfo addInfo(int killedObjectId, int killerObjectId, int deathTime, String killedName, String killerName, int killedLevel, int killerLevel, int killedClassId, int killerClassId, String killedClanName, String killerClanName, int karma, int sharedTime) {
		if (!isExpired(deathTime)) {
			PvpbookInfo pvpbookInfo = new PvpbookInfo(this, killedObjectId, killerObjectId, deathTime, killedName, killerName, killedLevel, killerLevel, killedClassId, killerClassId, killedClanName, killerClanName, karma, sharedTime);
			infos.put(pvpbookInfo.getKillerObjectId(), pvpbookInfo);
			return pvpbookInfo;
		}
		return null;
	}

	public Collection<PvpbookInfo> getInfos(boolean withExpired) {
		if (withExpired)
			return infos.values();

		List<PvpbookInfo> tempInfos = new ArrayList<>();
		for (PvpbookInfo pvpbookInfo : infos.values()) {
			if (!pvpbookInfo.isExpired())
				tempInfos.add(pvpbookInfo);
		}
		return tempInfos;
	}

	public PvpbookInfo getInfo(String killerName) {
		for (PvpbookInfo pvpbookInfo : getInfos(false)) {
			if (pvpbookInfo.getKillerName().equalsIgnoreCase(killerName))
				return pvpbookInfo;
		}
		return null;
	}

	public PvpbookInfo getInfo(int objectId) {
		PvpbookInfo pvpbookInfo = infos.get(objectId);
		if (pvpbookInfo != null && !pvpbookInfo.isExpired())
			return pvpbookInfo;
		return null;
	}

	public int getLocationShowCount() {
		return MAX_LOCATION_SHOW_COUNT_PER_DAY - owner.getVariables().getInt(LOCATION_SHOW_COUNT_VAR, 0);
	}

	public void reduceLocationShowCount() {
		int count = owner.getVariables().getInt(LOCATION_SHOW_COUNT_VAR, 0);
		owner.getVariables().set(LOCATION_SHOW_COUNT_VAR, count + 1);
	}

	public int getTeleportCount() {
		return MAX_TELEPORT_COUNT_PER_DAY - owner.getVariables().getInt(TELEPORT_COUNT_VAR, 0);
	}
	
	public int getTeleportHelpCount()
	{
		return MAX_TELEPORT_HELP_COUNT_PER_DAY - owner.getVariables().getInt(TELEPORT_HELP_COUNT_VAR, 0);
	}

	public void reduceTeleportCount() {
		int count = owner.getVariables().getInt(TELEPORT_COUNT_VAR, 0);
		owner.getVariables().set(TELEPORT_COUNT_VAR, count + 1);
	}
	
	public void reduceTeleportHelpCount()
	{
		int count = owner.getVariables().getInt(TELEPORT_HELP_COUNT_VAR, 0);
		owner.getVariables().set(TELEPORT_HELP_COUNT_VAR, count + 1);
	}

	public void reset() {
		owner.getVariables().remove(LOCATION_SHOW_COUNT_VAR);
		owner.getVariables().remove(TELEPORT_COUNT_VAR);
		owner.getVariables().remove(TELEPORT_HELP_COUNT_VAR);
	}

	public static boolean isExpired(int deathTime) {
		return (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - deathTime) > EXPIRATION_DELAY;
	}
}
