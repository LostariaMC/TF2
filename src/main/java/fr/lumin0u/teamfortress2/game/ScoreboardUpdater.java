package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.util.Utils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ScoreboardUpdater
{
	public String getFormattedTimer() {
		return new SimpleDateFormat("mm':'ss").format(new Date(System.currentTimeMillis() - GameManager.getInstance().getStartDate()));
	}
	
	public CosmoxScoreboard createScoreboard(TFPlayer player) {
		CosmoxScoreboard scoreboard = new CosmoxScoreboard(player.toBukkit());
		scoreboard.updateTitle("§f§lTF2");
		scoreboard.updateLine(0, "§0");
		scoreboard.updateLine(1, "§6| §eTimer §f━ §e" + getFormattedTimer());
		
		player.toCosmox().setScoreboard(scoreboard);
		
		updateClass(player);
		
		return scoreboard;
	}
	
	public void updateTimer() {
		for(WrappedPlayer watcher : WrappedPlayer.of(Bukkit.getOnlinePlayers())) {
			watcher.toCosmox().getScoreboard().updateLine(1, "§6| §eTimer §f━ §e" + getFormattedTimer());
		}
	}
	
	public void updateClass(TFPlayer player) {
		player.toCosmox().getScoreboard().updateLine(3, "§6| §7Classe §f━ " + Utils.bungeeColor(player.getKit().getColor()) + player.getKit().getName() + " " + player.getKit().getSymbol());
	}
}