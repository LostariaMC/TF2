package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.worsewarn.cosmox.game.events.PlayerJoinGameEvent;
import fr.worsewarn.cosmox.game.events.PlayerJoinTeamEvent;
import fr.worsewarn.cosmox.game.teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class ConnexionListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public ConnexionListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		if(!player.hasJoinedBefore()) {
			player.toBukkit().setGameMode(GameMode.SPECTATOR);
			if(Bukkit.getOnlinePlayers().size() > 1)
				player.toBukkit().teleport(Bukkit.getOnlinePlayers().stream().filter(player::is).toList().get(0));
		}
		else {
			player.respawn(gm.findSpawnLocation(player));
		}
		
		gm.getScoreboardUpdater().createScoreboard(player);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		player.die(null, true);
		if(!player.isSpectator())
			player.setHasJoinedGameBefore(true);
	}
	
	@EventHandler
	public void onJoinTeam(PlayerJoinTeamEvent event) {
		if(event.getTeam().equals(Team.SPEC))
			return;
		
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		gm.getTFTeam(event.getTeam()).ifPresent(player::setTeam);
		
		player.respawn(gm.findSpawnLocation(player));
	}
	
	@EventHandler
	public void onRainJoins(WeatherChangeEvent event) {
		if(event.toWeatherState())
			event.setCancelled(true);
	}
}
