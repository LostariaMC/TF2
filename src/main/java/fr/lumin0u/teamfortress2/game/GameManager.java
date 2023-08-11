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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public abstract class GameManager
{
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
	 * */
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		phase = GamePhase.END;
		
		API.instance().getManager().getGame().addToResume(Messages.SUMMARY_WIN.formatted(winnerTeam == null ? winner.getName() : winnerTeam.cosmoxTeam().getPrefix()));
		API.instance().getManager().getGame().addToResume(Messages.SUMMARY_TIME.formatted(new SimpleDateFormat("mm':'ss").format(new Date(System.currentTimeMillis() - startDate))));
		
		API.instance().getManager().setPhase(Phase.END);
	}
	
	public void createScoreboard(TFPlayer player) {
		CosmoxScoreboard scoreboard = new CosmoxScoreboard(player.toBukkit());
		scoreboard.updateTitle("§f§lTF2");
		
		player.toCosmox().setScoreboard(scoreboard);
	}
	
	public void updateScoreboard() {
		tf.getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(this::updateScoreboard);
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
