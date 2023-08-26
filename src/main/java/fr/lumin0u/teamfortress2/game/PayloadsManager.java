package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PayloadsManager extends GameManager
{
	public static Iterator<Block> getRailsIteratorBetween(Block start, Block end) {
		return new Iterator<Block>() {
			private Block next = start;
			private Vector lastDirection = null;
			private static final List<Vector> directions = List.of(new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(1, 1, 0), new Vector(-1, 1, 0), new Vector(0, 1, 1), new Vector(0, 1, -1), new Vector(1, -1, 0), new Vector(-1, -1, 0), new Vector(0, -1, 1), new Vector(0, -1, -1));
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
			
			@Override
			public Block next() {
				if(next == null)
					throw new NoSuchElementException();
				
				Block current = next;
				Block sameDirection;
				
				if(lastDirection != null && (sameDirection = current.getRelative(lastDirection.getBlockX(), lastDirection.getBlockY(), lastDirection.getBlockZ())).getType().equals(Material.RAIL)) {
					next = sameDirection;
				}
				else {
					next = null;
					for(Vector direction : directions) {
						if(lastDirection != null && direction.clone().add(lastDirection).isZero())
							continue;
						Block relative = current.getRelative(direction.getBlockX(), direction.getBlockY(), direction.getBlockZ());
						if(relative.getType().equals(Material.RAIL)) {
							next = relative;
							lastDirection = direction.clone();
							break;
						}
					}
				}
				current.getWorld().spawnParticle(Particle.REDSTONE, current.getLocation().toCenterLocation(), 1, 0, 0, 0, 0, new DustOptions(Color.RED, 1));
				return current;
			}
		};
	}
	
	private final Map<TFTeam, Minecart> minecarts = new HashMap<>();
	private final Map<TFTeam, Integer> maxRailIndex = new HashMap<>();
	private final Map<TFTeam, Material> coloredBlocks = new HashMap<>();
	private final Map<TFTeam, Material> notColoredBlocks = new HashMap<>();
	
	public PayloadsManager(GameMap map) {
		super(map, List.of(TFTeam.loadPayloads(Team.RED, map), TFTeam.loadPayloads(Team.BLUE, map)), GameType.PAYLOADS, new PayloadsScoreboardUpdater());
		
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		teams.forEach(team -> {
			org.bukkit.scoreboard.Team bukkitTeam = board.registerNewTeam(team.getName(false));
			bukkitTeam.color(team.cosmoxTeam() == Team.RED ? NamedTextColor.RED : NamedTextColor.BLUE);
		});
	}
	
	public static PayloadsManager getInstance() {
		return (PayloadsManager) TF.getInstance().getGameManager();
	}
	
	@Override
	public void preStartGame() {
		super.preStartGame();
		
		teams.forEach(team -> {
			Minecart cart = (Minecart) map.getWorld().spawnEntity(team.getRailsStart().getLocation().add(0.5, 0, 0.5), EntityType.MINECART);
			cart.setDisplayBlockData(team.getBlockInCart().createBlockData());
			Bukkit.getScoreboardManager().getMainScoreboard().getTeam(team.getName(false)).addEntity(cart);
			cart.setGlowing(true);
			
			minecarts.put(team, cart);
			maxRailIndex.put(team, 0);
			coloredBlocks.put(team, team.getRailsStart().getRelative(BlockFace.DOWN).getType());
			notColoredBlocks.put(team, team.getRailsEnd().getRelative(BlockFace.DOWN).getType());
			
			team.getOnlinePlayers().forEach(player -> {
				player.respawn(team.getSpawnpoint());
			});
		});
	}
	
	@Override
	public void startGame() {
		super.startGame();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(phase.isInGame()) {
					teams.forEach(team -> {
						
						Minecart cart = minecarts.get(team);
						List<Block> rails = team.getRails();
						final int railIndex = rails.indexOf(cart.getLocation().getBlock());
						Block currentRail = cart.getLocation().getBlock();
						
						final double CART_SPEED = (double) rails.size() / 4200.0;
						
						if(railIndex == -1) {
							System.out.println(team.getName(false) + "'s CART IS OUT OF RAILS !!");
							return;
						}
						
						Vector forwards = railIndex == rails.size() - 1 ? new Vector() : rails.get(railIndex + 1).getLocation().subtract(currentRail.getLocation()).toVector();
						Vector backwards = railIndex == 0 ? new Vector() : currentRail.getLocation().subtract(rails.get(railIndex - 1).getLocation()).toVector();
						
						double push;
						
						if(teams.stream()
								.filter(t -> !t.equals(team))
								.flatMap(t -> t.getOnlinePlayers().stream())
								.anyMatch(p -> p.toBukkit().getLocation().distance(cart.getLocation()) < 3)) {
							push = -1;
						}
						else {
							push = Math.sqrt(team.getOnlinePlayers().stream()
									.filter(p -> p.toBukkit().getLocation().distance(cart.getLocation()) < 3)
									.mapToDouble(p -> p.getKit().getValeurCart())
									.sum());
						}
						
						if(push > 0) {
							if(push >= 1.999 || forwards.getY() <= 0)
								cart.setVelocity(forwards.clone().multiply(push * CART_SPEED));
							else
								cart.setVelocity(new Vector());
						}
						/*else if(push == 0 && railIndex > 0 && backwards.getY() < 0) {
							cart.setVelocity(backwards.clone().multiply(2 * CART_SPEED));
							currentRail.getRelative(BlockFace.DOWN).setType(notColoredBlocks.get(team), false);
						}*/
						else {
							cart.setVelocity(cart.getVelocity().multiply(0.98));
						}
						
						if(!cart.getVelocity().isZero()) {
							if(cart.getVelocity().dot(forwards) > 0)
								currentRail.getRelative(BlockFace.DOWN).setType(coloredBlocks.get(team), false);
							else
								currentRail.getRelative(BlockFace.DOWN).setType(notColoredBlocks.get(team), false);
						}
						
						if(railIndex > maxRailIndex.get(team)) {
							double lastProgression = (double) maxRailIndex.get(team) / (double) (rails.size() - 1);
							double newProgression = (double) railIndex / (double) (rails.size() - 1);
							
							maxRailIndex.put(team, railIndex);
							onCartProgresses(team, lastProgression, newProgression);
						}
					});
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	@Override
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		super.endGame(winner, winnerTeam);
		
		TF.getInstance().getNonSpecPlayers().forEach(player -> {
			if(winnerTeam.equals(player.getTeam())) {
				player.toCosmox().addMolecules(ExpValues.WIN_PAYLOADS, "Victoire");
				player.toCosmox().addStatistic(GameVariables.WIN, 1);
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false, true));
			}
			else {
				player.toCosmox().addMolecules(ExpValues.LOSE_PAYLOADS, "Lot de consolation");
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
	
	public void onCartProgresses(TFTeam team, double lastProgression, double newProgression) {
		if((int) (lastProgression * 10) < (int) (newProgression * 10)) {
			team.getPlayers().forEach(player -> player.toCosmox().addMolecules(ExpValues._10PERCENT_PAYLOADS, "" + ((int) (newProgression*10) * 10) + "%"));
		}
		
		team.setPayloadProgression(newProgression);
		((PayloadsScoreboardUpdater) scoreboardUpdater).updateTeamPercentage(team);
		
		if(newProgression >= 1) {
			endGame(null, team);
		}
	}
	
	@Override
	public Location findSpawnLocation(TFPlayer player) {
		return player.getTeam().getSpawnpoint();
	}
	
	public static class PayloadsScoreboardUpdater extends ScoreboardUpdater
	{
		@Override
		public CosmoxScoreboard createScoreboard(TFPlayer player) {
			player.toBukkit().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			
			CosmoxScoreboard scoreboard = super.createScoreboard(player);
			
			scoreboard.updateLine(2, "§6| §eMode §f━ §e§lPayloads");
			
			scoreboard.updateLine(4, "§2");
			
			AtomicInteger line = new AtomicInteger(5);
			
			PayloadsManager.getInstance().getTeams().stream().sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills())).forEach(team -> {
				scoreboard.updateLine(line.getAndIncrement(),
						"§7Equipe " + team.getChatColor() + (team.equals(player.getTeam()) ? "§l" : "") + team.getName(true)
								+ "§7: §a%d%%".formatted((int) (team.getPayloadProgression() * 100)));
			});
			scoreboard.updateLine(line.getAndIncrement(), "§e");
			scoreboard.updateLine(line.getAndIncrement(), "§f");
			
			return scoreboard;
		}
		
		public void updateTeamPercentage(TFTeam team) {
			Bukkit.getOnlinePlayers().stream().map(TFPlayer::of).forEach(watcher -> {
				CosmoxScoreboard scoreboard = watcher.toCosmox().getScoreboard();
				
				AtomicInteger line = new AtomicInteger(5);
				PayloadsManager.getInstance().getTeams().stream()
						.sorted((t1, t2) -> -Integer.compare(t1.getKills(), t2.getKills()))
						.forEach(t -> {
							scoreboard.updateLine(line.getAndIncrement(),
									"§7Equipe " + t.getChatColor() + (t.equals(watcher.getTeam()) ? "§l" : "") + t.getName(true)
											+ "§7: §a%d%%".formatted((int) (team.getPayloadProgression() * 100)));
						});
			});
		}
	}
}
