package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TDMManager extends GameManager
{
	public static final int KILLS_TO_WIN = 20;
	
	public TDMManager(GameMap map) {
		super(map, List.of(TFTeam.loadDefault(Team.RED, map), TFTeam.loadDefault(Team.BLUE, map)), GameType.TEAM_DEATHMATCH, new TDMScoreboardUpdater());
	}
	
	public static TDMManager getInstance() {
		return (TDMManager) TF.getInstance().getGameManager();
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
	
	public void onSingleKill(TFPlayer victim, TFPlayer damager) {
		if(damager != null) {
			damager.toCosmox().addMolecules(ExpValues.KILL_TDM, "Kill");
			damager.getTeam().incrementKills();
			((TDMScoreboardUpdater) scoreboardUpdater).updateTeamKills(damager.getTeam());
			
			if(damager.getTeam().getKills() >= KILLS_TO_WIN) {
				endGame(null, damager.getTeam());
			}
		}
	}
	
	public static class TDMScoreboardUpdater extends ScoreboardUpdater
	{
		@Override
		public CosmoxScoreboard createScoreboard(TFPlayer player) {
			CosmoxScoreboard scoreboard = super.createScoreboard(player);
			
			scoreboard.updateLine(2, "§6| §eMode §f━ §e§lTeam Deathmatch");
			
			scoreboard.updateLine(4, "§2");
			
			AtomicInteger line = new AtomicInteger(5);
			
			TDMManager.getInstance().getTeams().stream().sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills())).forEach(team -> {
				scoreboard.updateLine(line.getAndIncrement(),
						"§7Equipe " + team.getChatColor() + (team.equals(player.getTeam()) ? "§l" : "") + team.getName(true)
								+ "§7: §a" + team.getKills() + " §2/%d".formatted(KILLS_TO_WIN));
			});
			scoreboard.updateLine(line.getAndIncrement(), "§e");
			scoreboard.updateLine(line.getAndIncrement(), "§f");
			
			player.toBukkit().setLevel(0);
			player.toBukkit().setExp(0);
			
			return scoreboard;
		}
		
		public void updateTeamKills(TFTeam team) {
			Bukkit.getOnlinePlayers().stream().map(TFPlayer::of).forEach(watcher -> {
				CosmoxScoreboard scoreboard = watcher.toCosmox().getScoreboard();
				watcher.toBukkit().setExp((float) watcher.getTeam().getKills() / (float) KILLS_TO_WIN);
				
				AtomicInteger line = new AtomicInteger(5);
				TDMManager.getInstance().getTeams().stream()
						.sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills()))
						.forEach(t -> {
							scoreboard.updateLine(line.getAndIncrement(),
									"§7Equipe " + t.getChatColor() + (t.equals(watcher.getTeam()) ? "§l" : "") + t.getName(true)
											+ "§7: §a" + t.getKills() + " §2/%d".formatted(KILLS_TO_WIN));
						});
			});
		}
	}
}
