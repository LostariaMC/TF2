package fr.lumin0u.teamfortress2.game.managers;

import fr.lumin0u.teamfortress2.TF;
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
import net.kyori.adventure.text.Component;
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
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

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
			//Bukkit.getScoreboardManager().getMainScoreboard().getTeam(team.getName(false)).addEntity(cart);
			cart.setGlowing(true);
			
			minecarts.put(team, cart);
			maxRailIndex.put(team, 0);
			coloredBlocks.put(team, team.getRailsStart().getRelative(BlockFace.DOWN).getType());
			notColoredBlocks.put(team, team.getRailway().get(1).getRelative(BlockFace.DOWN).getType());
			
			team.getOnlinePlayers().forEach(player -> {
				player.respawn(team.getSpawnpoint());
			});
		});
		
		getPlayers().forEach(this::showCartsGlowing);
		
		Bukkit.broadcast(Component.text()
				.append(Component.text(TF.getInstance().getCosmoxGame().getPrefix()))
				.append(Component.text("§eMode de jeu §aPayloads §e! Vous devez pousser le §aminecart de votre couleur §ele long des rails jusqu'au bout pour gagner."))
				.build());
		Bukkit.broadcast(Component.text()
				.append(Component.text(TF.getInstance().getCosmoxGame().getPrefix()))
				.append(Component.text("§eLe minecart est lourd ! Il faut une §avaleur minecart supérieure ou égale à 2 §epour le pousser dans les montées." +
						" Certains kits (§aspy §eet §ascout§e) ont une valeur de 2 par défaut. Plusieurs joueurs autour du wagon ? Les valeurs s'ajoutent !"))
				.build());
		Bukkit.broadcast(Component.text()
				.append(Component.text(TF.getInstance().getCosmoxGame().getPrefix()))
				.append(Component.text("§eAttention, si un ennemi est lui aussi proche de votre wagon, celui-ci s'arrete !"))
				.build());
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
						List<Block> railway = team.getRailway();
						final int railIndex = railway.indexOf(cart.getLocation().getBlock());
						Block currentRail = cart.getLocation().getBlock();
						World world = currentRail.getWorld();
						
						final double CART_SPEED = 0.04;
						
						if(railIndex == -1) {
							System.out.println(team.getName(false) + "'s CART IS OUT OF RAILS !!");
							if(cart.getVelocity().isZero())
								cart.setVelocity(Vector.getRandom().multiply(new Vector(new Random().nextBoolean() ? 1 : -1, -1, new Random().nextBoolean() ? 1 : -1)));
							return;
						}
						
						Vector forwards = railIndex == railway.size() - 1 ? new Vector() : railway.get(railIndex + 1).getLocation().subtract(currentRail.getLocation()).toVector();
						Vector backwards = railIndex == 0 ? new Vector() : currentRail.getLocation().subtract(railway.get(railIndex - 1).getLocation()).toVector();
						
						double push;
						
						if(teams.stream()
								.filter(t -> !t.equals(team))
								.flatMap(t -> t.getOnlinePlayers().stream())
								.filter(Predicate.not(TFPlayer::isDead))
								.anyMatch(p -> p.toBukkit().getLocation().distance(cart.getLocation()) < 3)) {
							push = -1;
						}
						else {
							push = Math.sqrt(2 * team.getOnlinePlayers().stream()
									.filter(Predicate.not(TFPlayer::isDead))
									.filter(p -> p.toBukkit().getLocation().distance(cart.getLocation()) < 3)
									.filter(p -> world.rayTraceBlocks(p.getEyeLocation(), cart.getLocation().add(0, 0.5, 0).subtract(p.getEyeLocation()).toVector(), p.getEyeLocation().distance(cart.getLocation().add(0, 0.5, 0))) == null)
									.mapToDouble(p -> p.getKit().getValeurCart())
									.sum());
						}
						
						if(push > 0) {
							if(push >= 1.999 || forwards.getY() <= 0) {
								cart.setVelocity(forwards.clone().multiply(push * CART_SPEED));
							}
							else {
								cart.setVelocity(new Vector());
							}
						}
						else if(push < 0 && cart.getVelocity().dot(forwards) > 0) {
							cart.setVelocity(new Vector());
						}
						else {
							cart.setVelocity(cart.getVelocity().multiply(0.99));
						}
						
						if(!cart.getVelocity().isZero()) {
							if(cart.getVelocity().dot(forwards) > 0)
								currentRail.getRelative(BlockFace.DOWN).setType(coloredBlocks.get(team), false);
							else
								currentRail.getRelative(BlockFace.DOWN).setType(notColoredBlocks.get(team), false);
						}
						
						if(railIndex > maxRailIndex.get(team)) {
							int lastMaxRail = maxRailIndex.get(team);
							
							maxRailIndex.put(team, railIndex);
							onCartProgresses(team, lastMaxRail, railIndex, railway.size() - 1);
						}
					});
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
		
		final int TIME_BTWN_XP = 3 * 60 * 20;
		Bukkit.getScheduler().runTaskTimer(tf, () -> {
			if(phase.isInGame())
				getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(p -> p.toCosmox().addMolecules(ExpValues.ADDITIONAL_PER_MINUTE_PAYLOADS * (double) TIME_BTWN_XP / 20 / 60, "Temps de jeu"));
		}, TIME_BTWN_XP, TIME_BTWN_XP);
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
	
	public void onCartProgresses(TFTeam team, int lastRailMax, int newRailMax, int railwayLength) {
		double lastProgression = (double) lastRailMax / (double) railwayLength;
		double newProgression = (double) newRailMax / (double) railwayLength;
		
		// new 10% bar !
		if((int) (lastProgression * 10) < (int) (newProgression * 10)) {
			double railCount = (double) railwayLength / 10;
			team.getPlayers().forEach(player -> player.toCosmox().addMolecules(ExpValues._100_RAILS_PAYLOADS * railCount / 100, "" + (int) (newProgression * 100) + "%"));
		}
		
		team.setPayloadProgression(newProgression);
		((PayloadsScoreboardUpdater) scoreboardUpdater).updateTeamPercentage(team);
		
		if(newProgression >= 1) {
			/*double remainingRails = (double) (railwayLength % 100);
			team.getPlayers().forEach(player -> player.toCosmox().addMolecules(ExpValues._100RAILS_PAYLOADS * remainingRails / 100, "100%"));*/
			endGame(null, team);
		}
	}
	
	@Override
	public Location findSpawnLocation(TFPlayer player) {
		return player.getTeam().getSpawnpoint();
	}
	
	public void showCartsGlowing(TFPlayer player) {
		getInstance().teams.forEach(team -> {
			if(minecarts.get(team) != null) {
				player.setEntityGlowing(minecarts.get(team), team.getName(false), team.cosmoxTeam() == Team.RED ? NamedTextColor.RED : NamedTextColor.BLUE);
			}
		});
	}
	
	public static class PayloadsScoreboardUpdater extends ScoreboardUpdater
	{
		private PayloadsScoreboardUpdater() {}
		
		@Override
		public CosmoxScoreboard createScoreboard(TFPlayer player) {
			CosmoxScoreboard scoreboard = super.createScoreboard(player);
			
			getInstance().showCartsGlowing(player);
			
			scoreboard.updateLine(2, "§6| §eMode §f━ §e§lPayloads");
			
			scoreboard.updateLine(4, "§2");
			
			AtomicInteger line = new AtomicInteger(5);
			
			PayloadsManager.getInstance().getTeams().forEach(t -> {
				scoreboard.updateLine(line.getAndIncrement(),
						"§7Equipe " + t.getChatColor() + (t.equals(player.getTeam()) ? "§l" : "") + t.getName(true) + "§7: §a0%");
			});
			scoreboard.updateLine(line.getAndIncrement(), "§e");
			scoreboard.updateLine(line.getAndIncrement(), "§f");
			
			player.toBukkit().setLevel(0);
			player.toBukkit().setExp(0);
			
			return scoreboard;
		}
		
		public void updateTeamPercentage(TFTeam team) {
			Bukkit.getOnlinePlayers().stream().map(TFPlayer::of).forEach(watcher -> {
				CosmoxScoreboard scoreboard = watcher.toCosmox().getScoreboard();
				
				watcher.toBukkit().setExp((float) watcher.getTeam().getPayloadProgression());
				
				AtomicInteger line = new AtomicInteger(5);
				PayloadsManager.getInstance().getTeams().stream()
						.sorted((t1, t2) -> -Double.compare(t1.getPayloadProgression(), t2.getPayloadProgression()))
						.forEach(t -> {
							scoreboard.updateLine(line.getAndIncrement(),
									"§7Equipe " + t.getChatColor() + (t.equals(watcher.getTeam()) ? "§l" : "") + t.getName(true)
											+ "§7: §a%d%%".formatted((int) (t.getPayloadProgression() * 100)));
						});
			});
		}
	}
}
