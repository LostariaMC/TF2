package fr.lumin0u.teamfortress2.game.managers;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.game.ScoreboardUpdater;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.TFTeam;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class KothManager extends GameManager
{
	public static final long TIME_TO_WIN = 3 * 60 * 20;
	
	private static final String WAITING_CAPTURE_S = "§7En attente de capture";
	private static final String CAPTURING_S = "%sCapture en cours...";
	private static final String CAPTURED_S = "§7Capturé par l'équipe %s";
	private static final String CONTESTED_S = "§7(contesté)";
	
	private final BossBar bossBar;
	private final BoundingBox captureZone;
	
	public KothManager(GameMap map) {
		super(map, List.of(TFTeam.loadDefault(Team.RED, map), TFTeam.loadDefault(Team.BLUE, map)), GameType.KOTH, new KothScoreboardUpdater());
		
		bossBar = BossBar.bossBar(Component.text(WAITING_CAPTURE_S), 0, Color.WHITE, Overlay.PROGRESS);
		captureZone = BoundingBox.of(map.getCuboid("kothMiddle").get(0), map.getCuboid("kothMiddle").get(1));
	}
	
	public static KothManager getInstance() {
		return (KothManager) TF.getInstance().getGameManager();
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
		
		new BukkitRunnable() {
			TFTeam capturer;
			int captureTime;
			int tick;
			
			@Override
			public void run() {
				if(phase.isInGame()) {
					
					if(tick++ % 20 == 0) {
						getOnlinePlayers().stream()
								.filter(not(TFEntity::isDead))
								.filter(p -> captureZone.contains(p.getLocation().toVector()))
								.forEach(p -> p.toCosmox().addStatistic("captureTime", 1));
					}
					
					Map<TFTeam, Integer> playerCountIn = getTeams().stream()
							.collect(Collectors.toMap(Function.identity(), t -> (int) t.getPlayers().stream()
									.filter(not(TFEntity::isDead))
									.filter(p -> captureZone.contains(p.getLocation().toVector()))
									.count()));
					
					List<TFTeam> sorted = playerCountIn.keySet().stream()
							.sorted((t1, t2) -> -Integer.compare(playerCountIn.get(t1), playerCountIn.get(t2)))
							.toList();
					
					TFTeam newCapturer = sorted.get(0);
					
					if(playerCountIn.get(newCapturer) > playerCountIn.get(sorted.get(1)) && (newCapturer == capturer || capturer == null)) {
						captureTime++;
						
						if(capturer == null) {
							bossBar.name(Component.text(CAPTURING_S.formatted(newCapturer.getChatColor())));
							bossBar.color(newCapturer.cosmoxTeam() == Team.RED ? Color.RED : Color.BLUE);
							bossBar.progress(0);
							capturer = newCapturer;
						}
						
						if(captureTime == 40) {
							bossBar.name(Component.text(CAPTURED_S.formatted(newCapturer.getChatColor() + "§l" + newCapturer.getName(true))));
							bossBar.progress(1);
						}
						
						if(captureTime >= 40) {
							newCapturer.incrementKothCaptureTime();
							
							if(newCapturer.getKothCaptureTime() % (30 * 20) == 0) {
								newCapturer.getPlayers().forEach(p -> p.toCosmox().addMolecules(ExpValues._1_MINUTE_CAPTURE_KOTH / 2, "Capture"));
							}
							if(newCapturer.getKothCaptureTime() >= TIME_TO_WIN) {
								endGame(null, newCapturer);
								cancel();
							}
							
							if(captureTime % 10 == 0) {
								((KothScoreboardUpdater) scoreboardUpdater).updateCaptureTime(capturer);
							}
						}
						else {
							bossBar.progress((float) captureTime / 40.0f);
						}
					}
					else if(capturer != null && (playerCountIn.get(capturer) < playerCountIn.get(newCapturer) || playerCountIn.get(capturer) == 0)) {
						if(captureTime >= 40) {
							captureTime = 40;
							
							bossBar.name(Component.text(CAPTURING_S.formatted(capturer.getChatColor())));
						}
						
						if(captureTime > 0) {
							
							bossBar.progress((float) captureTime / 40.0f);
							captureTime--;
						}
						else {
							capturer = null;
							
							bossBar.name(Component.text(WAITING_CAPTURE_S));
							bossBar.color(Color.WHITE);
							bossBar.progress(0);
						}
					}
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	@Override
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		super.endGame(winner, winnerTeam);
		
		TF.getInstance().getNonSpecPlayers().forEach(player -> {
			if(winnerTeam.equals(player.getTeam())) {
				player.toCosmox().addMolecules(ExpValues.WIN_KOTH, "Victoire");
				player.toCosmox().addStatistic(GameVariables.WIN, 1);
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false, true));
			}
			else {
				player.toCosmox().addMolecules(ExpValues.LOSE_KOTH, "Lot de consolation");
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
	
	public static class KothScoreboardUpdater extends ScoreboardUpdater
	{
		@Override
		public CosmoxScoreboard createScoreboard(TFPlayer player) {
			CosmoxScoreboard scoreboard = super.createScoreboard(player);
			getInstance().bossBar.addViewer(player.toBukkit());
			
			scoreboard.updateLine(2, "§6| §eMode §f━ §e§lKOTH");
			
			scoreboard.updateLine(4, "§2");
			
			AtomicInteger line = new AtomicInteger(5);
			
			KothManager.getInstance().getTeams().stream().sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills())).forEach(team -> {
				scoreboard.updateLine(line.getAndIncrement(),
						"§7Equipe " + team.getChatColor() + (team.equals(player.getTeam()) ? "§l" : "") + team.getName(true)
								+ "§7: §a0 §2/100");
			});
			scoreboard.updateLine(line.getAndIncrement(), "§e");
			scoreboard.updateLine(line.getAndIncrement(), "§f");
			
			player.toBukkit().setLevel(0);
			player.toBukkit().setExp(0);
			
			return scoreboard;
		}
		
		public void updateCaptureTime(TFTeam team) {
			Bukkit.getOnlinePlayers().stream().map(TFPlayer::of).forEach(watcher -> {
				CosmoxScoreboard scoreboard = watcher.toCosmox().getScoreboard();
				
				watcher.toBukkit().setExp((float) watcher.getTeam().getKothCaptureTime() / (float) TIME_TO_WIN);
				
				AtomicInteger line = new AtomicInteger(5);
				KothManager.getInstance().getTeams().stream()
						.sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills()))
						.forEach(t -> {
							scoreboard.updateLine(line.getAndIncrement(),
									"§7Equipe " + t.getChatColor() + (t.equals(watcher.getTeam()) ? "§l" : "") + t.getName(true)
											+ "§7: §a" + (int) ((double) t.getKothCaptureTime() / (double) TIME_TO_WIN * 100) + " §2/100");
						});
			});
		}
	}
}
