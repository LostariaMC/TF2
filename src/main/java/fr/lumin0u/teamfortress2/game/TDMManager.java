package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.chat.Messages;
import fr.worsewarn.cosmox.tools.map.GameMap;
import fr.worsewarn.cosmox.tools.world.Cuboid;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TDMManager extends GameManager
{
	public TDMManager(GameMap map) {
		super(map, List.of(
				new TFTeam(Team.RED, Material.REDSTONE_BLOCK, map.getLocation("redSpawn"), BoundingBox.of(map.getCuboid("redSafeZone").get(0), map.getCuboid("redSafeZone").get(1))),
				new TFTeam(Team.BLUE, Material.DIAMOND_BLOCK, map.getLocation("blueSpawn"), BoundingBox.of(map.getCuboid("blueSafeZone").get(0), map.getCuboid("blueSafeZone").get(1)))
		), GameType.TEAM_DEATHMATCH);
	}
	
	public static TDMManager getInstance() {
		return (TDMManager) TF.getInstance().getGameManager();
	}
	
	@Override
	public void updateScoreboard(TFPlayer player) {
		CosmoxScoreboard scoreboard = player.toCosmox().getScoreboard();
		AtomicInteger line = new AtomicInteger();
		
		scoreboard.updateLine(line.getAndIncrement(), "§6Timer§7: " + getFormattedTimer());
		scoreboard.updateLine(line.getAndIncrement(), "§7Mode: §fTEAM DEATHMATCH");
		
		if(!player.isSpectator())
			scoreboard.updateLine(line.getAndIncrement(), "§6Kit§7: §a" + player.getKit().getName());
		
		scoreboard.updateLine(line.getAndIncrement(), "§2");
		getTeams().stream().sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills())).forEach(team -> {
			scoreboard.updateLine(line.getAndIncrement(),
					"§7Equipe " + team.getPrefix() + (team.equals(player.getTeam()) ? "§l" : "") + team.getName(true)
							+ "§7: §a" + team.getKills() + " §2/40");
		});
		scoreboard.updateLine(line.getAndIncrement(), "§e");
		scoreboard.updateLine(line.getAndIncrement(), "§f");
	}
	
	@Override
	public void preStartGame() {
		super.preStartGame();
		
		teams.forEach(team -> {
			team.getOnlinePlayers().forEach(player -> {
				player.respawn(team.getSpawnpoint());
			});
		});
	}
	
	@Override
	public void startGame() {
		super.startGame();
		
		teams.forEach(team -> {
			team.getOnlinePlayers().forEach(player -> {
			
			});
		});
	}
	
	@Override
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		super.endGame(winner, winnerTeam);
		
		TF.getInstance().getNonSpecPlayers().forEach(player -> {
			if(winnerTeam.equals(player.getTeam())) {
				player.toCosmox().addMolecules(ExpValues.WIN_TDM, "Victoire");
				player.toCosmox().addStatistic(GameVariables.WIN, 1);
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false, true));
			}
			else {
				player.toCosmox().addMolecules(ExpValues.LOSE_TDM, "Lot de consolation");
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 2, false, false, true));
			}
		});
		
		TF.getInstance().getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(player ->
		{
			if(!player.isSpectator()) {
				player.toCosmox().addStatistic(GameVariables.GAMES_PLAYED, 1);
			}
			
			player.toBukkit().sendTitle("§eFin de la partie !", "", 5, 30, 30);
		});
	}
	
	@Override
	public Location findSpawnLocation(TFPlayer player) {
		return player.getTeam().getSpawnpoint();
	}
	
	public void onSingleKill(TFPlayer victim, TFTeam damagerTeam) {
		updateScoreboard();
		
		if(damagerTeam != null) {
			damagerTeam.incrementKills();
		}
	}
}
