package fr.lumin0u.teamfortress2.game;

import fr.worsewarn.cosmox.tools.map.GameMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GameType//▒
{
	PAYLOADS("Payloads", 2, true, false, false) {
		@Override
		public GameManager createManager(GameMap map) {
			throw new UnsupportedOperationException();
		}
	},
	FFA("FFA", 1, false, true, false) {
		@Override
		public GameManager createManager(GameMap map) {
			throw new UnsupportedOperationException();
		}
	},
	TEAM_DEATHMATCH("Team Deathmatch", 2, true, false, false) {
		@Override
		public GameManager createManager(GameMap map) {
			return new TDMManager(map);
		}
	},
	KOTH("KOTH", 2, true, false, false) {
		@Override
		public GameManager createManager(GameMap map) {
			throw new UnsupportedOperationException();
		}
	},
	CTF("CTF", 2, true, false, false) {
		@Override
		public GameManager createManager(GameMap map) {
			throw new UnsupportedOperationException();
		}
	},
	;
	
	private final String name;
	private final int nbTeams;
	private final boolean hide;
	private final boolean teams;
	private final boolean friendlyFire;
	
	private GameType(String name, int nbTeams, boolean teams, boolean friendlyFire, boolean hide)
	{
		this.name = name;
		this.teams = teams;
		this.nbTeams = nbTeams;
		this.hide = hide;
		this.friendlyFire = friendlyFire;
	}

	public String getName()
	{
		return name;
	}

	/*public String get_Name()
	{
		return name.replaceAll(" ", "_");
	}
	
	public static GameType byName(String name)
	{
		for(GameType gt : values())
			if(gt.getName().equalsIgnoreCase(name.replaceAll("_", " ")))
				return gt;
		
		return null;
	}*/

	public boolean areTeamsActive()
	{
		return teams;
	}
	
	public int nbTeams()
	{
		return nbTeams;
	}
	
	public boolean isHidden()
	{
		return hide;
	}
	
	public static List<GameType> notHiddenValues()
	{
		return Arrays.stream(values()).filter(gt -> !gt.isHidden()).collect(Collectors.toList());
	}
	
	public boolean isTDM()
	{
		return equals(TEAM_DEATHMATCH);
	}
	
	public boolean isKoth()
	{
		return equals(KOTH);
	}
	
	public boolean isCTF()
	{
		return equals(CTF);
	}
	
	public boolean isCarts()
	{
		return equals(PAYLOADS);
	}
	
	public boolean isFFA()
	{
		return !teams;
	}
	
	public abstract GameManager createManager(GameMap map);
	
	public boolean isFriendlyFire()
	{
		return friendlyFire;
	}
}
