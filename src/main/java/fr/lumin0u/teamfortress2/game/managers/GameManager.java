package fr.lumin0u.teamfortress2.game.managers;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.game.ScoreboardUpdater;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.TFTeam;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.chat.Messages;
import fr.worsewarn.cosmox.tools.map.GameMap;
import org.bukkit.*;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public abstract class GameManager
{
	protected final TF tf;
	protected final GameMap map;
	protected final List<TFTeam> teams;
	
	protected GamePhase phase;
	protected long startDate;
	protected final boolean friendlyFire;
	protected final GameType gameType;
	protected final ScoreboardUpdater scoreboardUpdater;
	
	public GameManager(GameMap map, List<TFTeam> teams, GameType gameType, ScoreboardUpdater scoreboardUpdater) {
		this.tf = TF.getInstance();
		this.map = map;
		this.friendlyFire = gameType.isFriendlyFire();
		this.gameType = gameType;
		
		try {
			map.getWorld().setTime(Integer.parseInt(map.getStr("timeOfDay")));
		} catch(NumberFormatException ignore) {
		
		}
		map.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		map.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, true);
		map.getWorld().setDifficulty(Difficulty.NORMAL);
		
		this.teams = teams;
		
		for(TFTeam team : teams) {
			if(team.cosmoxTeam() != null) {
				WrappedPlayer.of(Bukkit.getOnlinePlayers()).stream().filter(p -> p.toCosmox().getTeam().equals(team.cosmoxTeam())).forEach(p -> p.to(TFPlayer.class).setTeam(team));
			}
		}
		
		this.scoreboardUpdater = scoreboardUpdater;
		
		Bukkit.getScheduler().runTaskTimer(tf, () -> {
			scoreboardUpdater.updateTimer();
			getOnlinePlayers().forEach(p -> p.toCosmox().addStatistic(GameVariables.TIME_PLAYED, 1));
		}, 20, 20);
	}
	
	public static GameManager getInstance() {
		return TF.getInstance().getGameManager();
	}
	
	public Optional<TFTeam> getTFTeam(Team team) {
		return getTeams().stream()
				.filter(tfTeam -> team.equals(tfTeam.cosmoxTeam()))
				.findFirst();
	}
	
	public GameMap getMap() {
		return map;
	}
	
	public GameType getGameType() {
		return gameType;
	}
	
	public ScoreboardUpdater getScoreboardUpdater() {
		return scoreboardUpdater;
	}
	
	public Collection<TFEntity> getLivingEntities() {
		return getOnlinePlayers().stream().filter(not(TFEntity::isDead)).map(TFEntity.class::cast).toList();
	}
	
	public Collection<TFPlayer> getOnlinePlayers() {
		return tf.getPlayers().stream().filter(not(TFPlayer::isSpectator)).filter(WrappedPlayer::isOnline).toList();
	}
	
	public Collection<TFPlayer> getPlayers() {
		return tf.getPlayers().stream().filter(not(TFPlayer::isSpectator)).toList();
	}
	
	public List<TFTeam> getTeams() {
		return teams;
	}
	
	public GamePhase getPhase() {
		return phase;
	}
	
	public boolean isFriendlyFire() {
		return friendlyFire;
	}
	
	public void preStartGame() {
		phase = GamePhase.PRE_START;
		
		WrappedPlayer.of(Bukkit.getOnlinePlayers()).forEach(tf::loadTFPlayer);
		getPlayers().forEach(player -> {
			scoreboardUpdater.createScoreboard(player);
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
		
		TF.getInstance().getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(player ->
		{
			if(!player.isSpectator()) {
				player.toCosmox().addStatistic(GameVariables.GAMES_PLAYED, 1);
			}
			
			player.toBukkit().sendTitle("§eFin de la partie !", "", 5, 30, 30);
		});
		
		API.instance().getManager().setPhase(Phase.END);
	}
	
	public long getStartDate() {
		return startDate;
	}
	
	public String getFormattedTimer() {
		return new SimpleDateFormat("mm':'ss").format(new Date(System.currentTimeMillis() - startDate));
	}
	
	public void onPlayerLeave(TFPlayer player) {
		
		if(phase == GamePhase.END) {
			return;
		}
		
		if(getOnlinePlayers().stream().filter(player::isNot).noneMatch(not(player::isEnemy))) {
			if(isFriendlyFire()) {
				throw new IllegalStateException();
			}
			else {
				switch((int) getTeams().stream().filter(team -> team.getOnlinePlayers().stream().allMatch(player::isNot)).count()) {
					case 0 -> {
						phase = GamePhase.END;
						API.instance().getManager().setPhase(Phase.END);
					}
					case 1 -> {
						endGame(null, getTeams().stream().filter(team -> !team.getOnlinePlayers().stream().allMatch(player::isNot)).findAny().get());
					}
				}
			}
		}
	}
	
	public void explosion(TFPlayer damager, Location loc, double centerDamage, double radius, Predicate<TFEntity> enemyPredicate, double centerKnockback) {
		
		TFSound.EXPLOSION.withVolume((int) (radius / 2.5)).play(loc);
		loc.getWorld().spawnParticle(radius > 5 ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0, null, true);
		
		int nbParticles = (int) (centerDamage * radius / 2);
		loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, nbParticles, 0, 0, 0, radius / 20, null, true);
		loc.getWorld().spawnParticle(Particle.FLAME, loc, nbParticles, 0, 0, 0, radius / 20, null, true);
		
		getLivingEntities().stream().filter(enemyPredicate).forEach(entity -> {
			double distance = entity.getLocation().distance(loc);
			if(distance < radius) {
				double damage = centerDamage * ((radius - distance) / radius);
				
				BlockIterator blocksBtwn = new BlockIterator(loc.getWorld(), loc.toVector(), loc.toVector().subtract(entity.getLocation().toVector()), 0, (int) distance + 1);
				
				AtomicInteger blockCount = new AtomicInteger();
				blocksBtwn.forEachRemaining(block -> {
					if(block.isBuildable())
						blockCount.incrementAndGet();
				});
				
				damage /= Math.max(1, (double) blockCount.get() + 0.5);
				
				Vector direction = entity.getLocation().toVector().subtract(loc.toVector());
				direction.normalize().multiply((radius - distance) / radius * centerKnockback);
				entity.damage(damager, damage, direction);
			}
		});
	}
	
	public abstract Location findSpawnLocation(TFPlayer player);
	
	public static enum GamePhase
	{
		PRE_START,
		GAME,
		END;
		
		public boolean isInGame() {
			return this == GAME;
		}
	}
}
