package org.l2jmobius.gameserver.model.pvpbook;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;

public class PvpbookInfo {
	private final Pvpbook pvpbook;
	private final int killedObjectId;
	private final int killerObjectId;
	private final int deathTime;
	private final int sharedTime;

	private String killedName;
	private String killerName;
	private int killedLevel;
	private int killerLevel;
	private int killedClassId;
	private int killerClassId;
	private String killedClanName;
	private String killerClanName;
	private int _karma;
	private boolean revenged;

	protected PvpbookInfo(Pvpbook pvpbook, Player killed, Player killer, int deathTime, int sharedTime) {
		this.pvpbook = pvpbook;
		killedObjectId = killed.getObjectId();
		killerObjectId = killer.getObjectId();
		this.deathTime = deathTime;
		killedName = killed.getName();
		killerName = killer.getName();
		killedLevel = killed.getLevel();
		killerLevel = killer.getLevel();
		killedClassId = killed.getClassId().getId();
		killerClassId = killer.getClassId().getId();
		Clan clan = killer.getClan();
		killerClanName = clan != null ? clan.getName() : "";
		clan = killed.getClan();
		killedClanName = clan != null ? clan.getName() : "";
		_karma = killer.getReputation();
		this.sharedTime = sharedTime;
	}

	protected PvpbookInfo(Pvpbook pvpbook, int killedObjectId, int killerObjectId, int deathTime, String killedName, String killerName, int killedLevel, int killerLevel, int killedClassId, int killerClassId, String killedClanName, String killerClanName, int karma, int sharedTime) {
		this.pvpbook = pvpbook;
		this.killedObjectId = killedObjectId;
		this.killerObjectId = killerObjectId;
		this.deathTime = deathTime;
		this.killedName = killedName;
		this.killerName = killerName;
		this.killedLevel = killedLevel;
		this.killerLevel = killerLevel;
		this.killedClassId = killedClassId;
		this.killerClassId = killerClassId;
		this.killedClanName = killedClanName;
		this.killerClanName = killerClanName;
		this._karma = karma;
		this.sharedTime = sharedTime;
	}
	
	public int getKilledObjectId()
	{
		return killedObjectId;
	}

	public int getKillerObjectId() {
		return killerObjectId;
	}
	
	public Player getKilled()
	{
		return World.getInstance().getPlayer(killedObjectId);
	}

	public Player getKiller()
	{
		return World.getInstance().getPlayer(killerObjectId);
	}

	public int getDeathTime() {
		return deathTime;
	}
	
	public String getKilledName() {
		Player player = getKilled();
		if (player != null)
			killedName = player.getName();
		return killedName;
	}

	public String getKillerName() {
		Player player = getKiller();
		if (player != null)
			killerName = player.getName();
		return killerName;
	}
	
	public int getKilledLevel() {
		Player player = getKilled();
		if (player != null)
			killedLevel = player.getLevel();
		return killedLevel;
	}

	public int getKillerLevel() {
		Player player = getKiller();
		if (player != null)
			killerLevel = player.getLevel();
		return killerLevel;
	}
	
	public int getKilledClassId() {
		Player player = getKilled();
		if (player != null)
			killedClassId = player.getClassId().getId();
		return killedClassId;
	}

	public int getKillerClassId() {
		Player player = getKiller();
		if (player != null)
			killerClassId = player.getClassId().getId();
		return killerClassId;
	}
	
	public String getKilledClanName() {
		Player player = getKilled();
		if (player != null) {
			Clan clan = player.getClan();
			killedClanName = clan != null ? clan.getName() : "";
		}
		return killedClanName;
	}

	public String getKillerClanName() {
		Player player = getKiller();
		if (player != null) {
			Clan clan = player.getClan();
			killerClanName = clan != null ? clan.getName() : "";
		}
		return killerClanName;
	}

	public boolean isOnline() {
		Player player = getKiller();
		if (player != null)
			return player.isOnline();
		return false;
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	public int getSharedTime()
	{
		return sharedTime;
	}

	public boolean isExpired() {
		return Pvpbook.isExpired(deathTime);
	}

	public boolean isRevenged() {
		return revenged;
	}

	public void setRevenged(boolean revenged) {
		this.revenged = revenged;
	}
}
