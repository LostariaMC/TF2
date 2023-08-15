package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.events.GameStartEvent;
import fr.worsewarn.cosmox.game.events.GameStopEvent;
import fr.worsewarn.cosmox.game.events.PlayerJoinGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CosmoxListener implements Listener
{
	private boolean gameStarted;
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		if(API.instance().getManager().getPhase().getState() == 0)
			return;
		
		Player player = event.getPlayer();
		player.setGameMode(GameMode.SPECTATOR);
		if(Bukkit.getOnlinePlayers().size() > 1)
			player.teleport(Bukkit.getOnlinePlayers().stream().filter(all -> all != player).toList().get(0));
		
		TF.getInstance().loadTFPlayer(WrappedPlayer.of(player));
		
		//if(GameManager.getInstance() != null)
		//	GameManager.getInstance().resetScoreboard(WrappedPlayer.of(player));
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(event.getGame().equals(TF.getInstance().getCosmoxGame())) {
			gameStarted = true;
			
			TF tf = TF.getInstance();
			
			GameManager gm = GameType.TEAM_DEATHMATCH.createManager(event.getMap());
			
			gm.preStartGame();
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), gm::startGame, 20L * TF.getInstance().getCosmoxGame().getPreparationTime());
			
			tf.registerListener(new MoveListener(gm, tf));
			tf.registerListener(new InteractListener(gm, tf));
			tf.registerListener(new DamageListener(gm, tf));
			tf.registerListener(new InventoryListener(gm, tf));
		}
	}
	
	@EventHandler
	public void onGameStop(GameStopEvent event) {
		
		if(gameStarted) {
			gameStarted = false;
			
			HandlerList.unregisterAll(TF.getInstance()); //Tous les évènements ne sont plus écoutés
			
			Bukkit.getScheduler().cancelTasks(TF.getInstance()); // arret de toutes les taches programmées du plugin
			
			TF.getInstance().reset();
			
			Bukkit.getPluginManager().registerEvents(this, TF.getInstance());
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getInventory().equals(Kit.getWRInventory())) {
			event.setCancelled(true);
			
			InventoryListener.onClickInChoseKitInventory(event);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction().isRightClick() && event.getItem() != null && event.getItem().isSimilar(TF.WR_KIT_ITEM)) {
			event.getPlayer().openInventory(Kit.getWRInventory());
		}
	}
}
