package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.tools.chat.Messages;
import fr.worsewarn.cosmox.tools.map.GameMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class GameManager {
	protected final TF tf;
	protected final GameMap map;
	protected final List<TFTeam> teams;
	
	protected GamePhase phase;
	protected long startDate;
	
	public GameManager(GameMap map, List<TFTeam> teams) {
		this.tf = TF.getInstance();
		this.map = map;
		
		this.teams = teams;
	}
	
	public static GameManager getInstance() {
		return TF.getInstance().getGameManager();
	}
	
	public GameMap getMap() {
		return map;
	}
	
	public Collection<? extends TFEntity> getEntities() {
		return tf.getPlayers();
	}
	
	public Collection<TFPlayer> getPlayers() {
		return tf.getPlayers();
	}
	
	public List<TFTeam> getTeams() {
		return teams;
	}
	
	public GamePhase getPhase() {
		return phase;
	}
	
	public void preStartGame() {
		phase = GamePhase.PRE_START;
		
		WrappedPlayer.of(Bukkit.getOnlinePlayers()).forEach(tf::loadTFPlayer);
		getPlayers().forEach(player -> {
			if(player.isSpectator())
				player.toBukkit().setGameMode(GameMode.SPECTATOR);
			else
				player.toBukkit().setGameMode(GameMode.ADVENTURE);
		});
	}
	
	public void startGame() {
		phase = GamePhase.GAME;
		
		startDate = System.currentTimeMillis();
	}
	
	/**
	 * depending on gameType, winner or winnerTeam should be passed and the other left as null
	 */
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		phase = GamePhase.END;
		
		API.instance().getManager().getGame().addToResume(Messages.SUMMARY_WIN.formatted(winnerTeam == null ? winner.getName() : winnerTeam.cosmoxTeam().getPrefix()));
		API.instance().getManager().getGame().addToResume(Messages.SUMMARY_TIME.formatted(getFormattedTimer()));
		
		API.instance().getManager().setPhase(Phase.END);
	}
	
	public long getStartDate() {
		return startDate;
	}
	
	public String getFormattedTimer() {
		return new SimpleDateFormat("mm':'ss").format(new Date(System.currentTimeMillis() - startDate));
	}
	
	public void createScoreboard(TFPlayer player) {
		CosmoxScoreboard scoreboard = new CosmoxScoreboard(player.toBukkit());
		scoreboard.updateTitle("§f§lTF2");
		
		player.toCosmox().setScoreboard(scoreboard);
	}
	
	public void updateScoreboard() {
		tf.getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(this::updateScoreboard);
	}
	
	public void explosion(TFPlayer damager, Location loc, double centerDamage, double radius, Predicate<TFEntity> ennemyPredicate, double centerKnockback) {
		
		loc.getWorld().spawnParticle(radius > 5 ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE, loc, 1);
		
		getEntities().stream().filter(ennemyPredicate).forEach(entity -> {
			double distance = entity.getLocation().distance(loc);
			if(distance < radius) {
				double damage = centerDamage * (1 - Math.pow(1 - (radius - distance) / radius, 2));
				
				BlockIterator blocksBtwn = new BlockIterator(
						loc.setDirection(entity.getLocation().toVector().subtract(loc.toVector())),
						0, (int) distance);
				
				AtomicInteger blockCount = new AtomicInteger();
				blocksBtwn.forEachRemaining(block -> {
					if(block.isSolid()) blockCount.incrementAndGet();
				});
				
				damage *= Math.max(1, (double) blockCount.get() / 2 + 0.5);
				
				Vector direction = entity.getLocation().toVector().subtract(loc.toVector());
				direction.normalize().multiply((radius - distance) / radius * centerKnockback);
				entity.damage(damager, damage, direction);
			}
		});
	}
	
	public abstract void updateScoreboard(TFPlayer player);
	
	public static enum GamePhase {
		PRE_START,
		GAME,
		END;
		
		public boolean isInGame() {
			return this == GAME;
		}
	}
}
