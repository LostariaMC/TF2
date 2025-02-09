package fr.lumin0u.teamfortress2.game.managers;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.game.ScoreboardUpdater;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.TFTeam;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FFAManager extends GameManager
{
	public static final int KILLS_TO_WIN = 20;

	private Map<Location, Integer> spawns;
	private List<Location> lastUsedSpawns = new ArrayList<>();

	public FFAManager(GameMap map) {
		super(map, List.of(TFTeam.loadDefault(Team.RANDOM, map)), GameType.FFA, new FFAScoreboardUpdater());
	}
	
	public static FFAManager getInstance() {
		return (FFAManager) TF.getInstance().getGameManager();
	}
	
	@Override
	public void preStartGame() {
		super.preStartGame();
		
		teams.forEach(team -> {
			team.getOnlinePlayers().forEach(player -> {
				player.respawn(findSpawnLocation(player));
			});
		});
	}
	
	@Override
	public void startGame() {
		super.startGame();

		((FFAScoreboardUpdater) scoreboardUpdater).leaderboard.addAll(getOnlinePlayers());
	}
	
	@Override
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		super.endGame(winner, winnerTeam);
		
		TF.getInstance().getNonSpecPlayers().forEach(player -> {
			if(winner.is(player)) {
				player.toCosmox().addMolecules(ExpValues.WIN_FFA, "Victoire");
				player.toCosmox().addStatistic(GameVariables.WIN, 1);
				if(player.isOnline())
					player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false, true));
			}
			else {
				player.toCosmox().addMolecules(ExpValues.LOSE_FFA, "Lot de consolation");
				if(player.isOnline())
					player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 2, false, false, true));
			}
		});
	}

	public List<Location> getAvailableSpawns()
	{
		return spawns.entrySet().stream()
				.filter(entry -> entry.getValue() <= getPlayers().size())
				.map(Map.Entry::getKey)
				.map(Location::clone)
				.collect(Collectors.toList());
	}

	@Override
	public Location findSpawnLocation(TFPlayer player)
	{
		Function<Location, Double> valueFunction = spawn -> getPlayers().stream()
				.filter(player::isNot)
				.mapToDouble(p -> Math.sqrt(p.toBukkit().getLocation().distance(spawn) * 10))
				.sum() - Math.pow(Collections.frequency(lastUsedSpawns, spawn) * 2, 2);

		Location chosenSpawn = getAvailableSpawns().stream()
				.max((spawn1, spawn2) -> Double.compare(valueFunction.apply(spawn1), valueFunction.apply(spawn2)))
				.get();

		lastUsedSpawns.add(chosenSpawn);

		if(lastUsedSpawns.size() > 3 * getPlayers().size())
			lastUsedSpawns.remove(0);

		return chosenSpawn;
	}
	
	public void onSingleKill(TFPlayer victim, TFPlayer damager) {
		if(damager != null) {
			damager.toCosmox().addMolecules(ExpValues.KILL_FFA, "Kill");
			((FFAScoreboardUpdater) scoreboardUpdater).updatePlayerRank(damager);
			
			if(damager.getTeam().getKills() >= KILLS_TO_WIN) {
				endGame(null, damager.getTeam());
			}
		}
	}
	
	public static class FFAScoreboardUpdater extends ScoreboardUpdater
	{
		private List<TFPlayer> leaderboard = new ArrayList<>();
		private int FIRST_LEADERBOARD_LINE = 6;

		@Override
		public CosmoxScoreboard createScoreboard(TFPlayer player) {
			CosmoxScoreboard scoreboard = super.createScoreboard(player);
			
			scoreboard.updateLine(2, "§6| §eMode §f━ §e§lFFA");
			
			scoreboard.updateLine(4, "§2");

			int line = 5;

			scoreboard.updateLine(line++, "§7| §b§lKills");

			final int MAX_LEADERBOARD_SIZE = 7-4;

			for(int i = 0; i < Math.min(MAX_LEADERBOARD_SIZE, leaderboard.size()); i++)
			{
				updatePlayerLine(leaderboard.get(i), line++);
			}

			if(leaderboard.size() > MAX_LEADERBOARD_SIZE)
			{
				scoreboard.updateLine(line++, "§7 . . .");

				if(leaderboard.indexOf(player) >= MAX_LEADERBOARD_SIZE)
				{
					int index = leaderboard.indexOf(player) + 1;

					if(!player.isSpectator())
					{
						scoreboard.updateLine(line++, "§8%d. %s%s§7: %d".formatted(index, "§d", player.getName(), player.getKillCount()));
					}
					else
					{
						scoreboard.updateLine(line++, "§8§n%d. %s§n%s§7§n: %d".formatted(index, "§d", player.getName(), player.getKillCount()));
					}
				}
			}

			scoreboard.updateLine(line++, "§1");
			scoreboard.updateLine(line++, "§7| " + ChatColor.of("#888888") + "Objectif: §e%d kills".formatted(KILLS_TO_WIN));
			scoreboard.updateLine(line++, "§2");

			scoreboard.updateLine(line++, "§7play.lostaria.fr");

			while(scoreboard.getLines().size() > line)
				scoreboard.removeLine(line++);

			return scoreboard;
		}

		public void updatePlayerLine(TFPlayer player, int line) {
			Bukkit.getOnlinePlayers().stream().map(TFPlayer::of).forEach(watcher -> {
				CosmoxScoreboard scoreboard = watcher.toCosmox().getScoreboard();

				int i = leaderboard.indexOf(player);
				String rankColor = (i == 0 ? ChatColor.of("#FFD700") : i == 1 ? ChatColor.of("#DBDBDB") : i == 2 ? ChatColor.of("#D9945D") : "§7") + "";

				String lineString = (rankColor + " §l" + (i + 1) + "§8. " + "§d" + "%s§7 - §f%d");

				scoreboard.updateLine(line, lineString.formatted(leaderboard.get(i).getName(), leaderboard.get(i).getKillCount()));
			});
		}
		
		public void updatePlayerRank(TFPlayer player) {

			int index = leaderboard.indexOf(player);
			int lastIndex = index;

			player.toBukkit().setExp((float) player.getKillCount() / (float) KILLS_TO_WIN);

			while (index != 0 && leaderboard.get(index - 1).getKillCount() <= player.getKillCount()) {
				leaderboard.add(index - 1, leaderboard.remove(index));

				if(index <= 2) {
					updatePlayerLine(leaderboard.get(index), index - 1 + FIRST_LEADERBOARD_LINE);
				}
				if(index - 1 <= 2) {
					updatePlayerLine(player, index + FIRST_LEADERBOARD_LINE);
				}

				index--;
			}
		}
	}
}
