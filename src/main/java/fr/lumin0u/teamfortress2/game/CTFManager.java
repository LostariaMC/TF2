package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.Flag.FlagState;
import fr.lumin0u.teamfortress2.game.PayloadsManager.PayloadsScoreboardUpdater;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CTFManager extends GameManager
{
	private final Map<TFTeam, Flag> flags;
	
	public CTFManager(GameMap map) {
		super(map, List.of(TFTeam.loadCTF(Team.RED, map), TFTeam.loadCTF(Team.BLUE, map)), GameType.CTF, new CTFScoreboardUpdater());
		flags = teams.stream().collect(Collectors.toMap(Function.identity(), Flag::new));
	}
	
	public static CTFManager getInstance() {
		return (CTFManager) GameManager.getInstance();
	}
	
	@Override
	public void preStartGame() {
		super.preStartGame();
		
		flags.values().forEach(Flag::spawnEntity);
		
		teams.forEach(team -> {
			team.getOnlinePlayers().forEach(player -> {
				player.respawn(team.getSpawnpoint());
			});
		});
		
		getPlayers().forEach(this::showFlagsGlowing);
		
		Bukkit.broadcast(Component.text()
				.append(Component.text(TF.getInstance().getCosmoxGame().getPrefix()))
				.append(Component.text("§eMode de jeu §aCapture de bannière §e! Vous devez capturer la §abannière adverse §eet la ramener auprès de la votre pour gagner des points."))
				.build());
		/*Bukkit.broadcast(Component.text()
				.append(Component.text(TF.getInstance().getCosmoxGame().getPrefix()))
				.append(Component.text("§"))
				.build());*/
	}
	
	@Override
	public void startGame() {
		super.startGame();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(phase.isInGame()) {
					flags.values().forEach(Flag::logic);
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
		
		final int TIME_BTWN_XP = 3 * 60 * 20;
		Bukkit.getScheduler().runTaskTimer(tf, () -> {
			if(phase.isInGame())
				getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(p -> p.toCosmox().addMolecules(ExpValues.ADDITIONAL_PER_MINUTE_CTF * (double) TIME_BTWN_XP / 20 / 60, "Temps de jeu"));
		}, TIME_BTWN_XP, TIME_BTWN_XP);
	}
	
	@Override
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		super.endGame(winner, winnerTeam);
		
		TF.getInstance().getNonSpecPlayers().forEach(player -> {
			if(winnerTeam.equals(player.getTeam())) {
				player.toCosmox().addMolecules(ExpValues.WIN_CTF, "Victoire");
				player.toCosmox().addStatistic(GameVariables.WIN, 1);
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false, true));
			}
			else {
				player.toCosmox().addMolecules(ExpValues.LOSE_CTF, "Lot de consolation");
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
	
	public void showFlagsGlowing(TFPlayer player) {
		flags.values().forEach(flag -> {
			if(flag.getFlagStand() != null)
				player.setEntityGlowing(flag.getFlagStand(), flag.getTeam().getName(false), flag.getTeam().cosmoxTeam() == Team.RED ? NamedTextColor.RED : NamedTextColor.BLUE);
		});
	}
	
	public Flag getFlag(TFTeam team) {
		return flags.get(team);
	}
	
	public Collection<Flag> getFlags() {
		return flags.values();
	}
	
	public void onPlayerDeath(TFPlayer dead) {
		flags.values().forEach(f -> f.onPlayerDeath(dead));
	}
	
	@Override
	public CTFScoreboardUpdater getScoreboardUpdater() {
		return (CTFScoreboardUpdater) scoreboardUpdater;
	}
	
	public static class CTFScoreboardUpdater extends ScoreboardUpdater
	{
		private CTFScoreboardUpdater() {}
		
		@Override
		public CosmoxScoreboard createScoreboard(TFPlayer player) {
			CosmoxScoreboard scoreboard = super.createScoreboard(player);
			
			getInstance().showFlagsGlowing(player);
			
			scoreboard.updateLine(2, "§6| §eMode §f━ §e§lPayloads");
			
			scoreboard.updateLine(4, "§2");
			
			AtomicInteger line = new AtomicInteger(5);
			
			getInstance().getTeams().forEach(t -> {
				scoreboard.updateLine(line.getAndIncrement(),
						"%s §a%d§2/3 %s".formatted(t.getChatColor() + (t.equals(player.getTeam()) ? "§l" : "") + t.getName(true), 0, FlagState.SAFE.getDisplay()));
			});
			scoreboard.updateLine(line.getAndIncrement(), "§e");
			scoreboard.updateLine(line.getAndIncrement(), "§f");
			
			player.toBukkit().setLevel(0);
			player.toBukkit().setExp(0);
			
			return scoreboard;
		}
		
		public void updateFlagState(TFTeam team) {
			Bukkit.getOnlinePlayers().stream().map(TFPlayer::of).forEach(watcher -> {
				CosmoxScoreboard scoreboard = watcher.toCosmox().getScoreboard();
				
				AtomicInteger line = new AtomicInteger(5);
				getInstance().getTeams()
						.forEach(t -> {
							scoreboard.updateLine(line.getAndIncrement(),
									"%s §a%d§2/3 %s".formatted(
											t.getChatColor() + (t.equals(watcher.getTeam()) ? "§l" : "") + t.getName(true),
											t.getFlagCaptureCount(),
											getInstance().flags.get(t).getState().getDisplay()));
						});
			});
		}
	}
}
