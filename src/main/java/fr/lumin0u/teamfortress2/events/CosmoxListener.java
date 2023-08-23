package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.GameType;
import fr.lumin0u.teamfortress2.util.Items;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.events.GameStartEvent;
import fr.worsewarn.cosmox.game.events.GameStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CosmoxListener implements Listener
{
	private boolean gameStarted;
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(event.getGame().equals(TF.getInstance().getCosmoxGame())) {
			gameStarted = true;
			
			TF tf = TF.getInstance();
			
			GameManager gm = tf.createGameManager(GameType.TEAM_DEATHMATCH, event.getMap());
			
			gm.preStartGame();
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
				gm.startGame();
				API.instance().getManager().setPhase(Phase.GAME);
			}, 20L * TF.getInstance().getCosmoxGame().getPreparationTime());
			
			tf.registerListener(new PlayerMovementListener(gm, tf));
			tf.registerListener(new InteractListener(gm, tf));
			tf.registerListener(new DamageListener(gm, tf));
			tf.registerListener(new InventoryListener(gm, tf));
			tf.registerListener(new ConnexionListener(gm, tf));
			tf.registerListener(new EntityMovementListener(gm, tf));
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
		if(!gameStarted && event.getInventory().equals(Kit.getWRInventory())) {
			event.setCancelled(true);
			
			Kit clickedKit = Kit.byRepItem(event.getCurrentItem());
			if(clickedKit != null) {
				TF.getInstance().setKitInRedis(WrappedPlayer.of(event.getWhoClicked()), clickedKit);
				event.getWhoClicked().sendMessage(TF.getInstance().getCosmoxGame().getPrefix() + "Vous choisissez la classe §e" + clickedKit.getName());
			}
			else if(Items.randomKitItem.isSimilar(event.getCurrentItem())) {
				TF.getInstance().setKitInRedis(WrappedPlayer.of(event.getWhoClicked()), Kit.RANDOM);
				event.getWhoClicked().sendMessage(TF.getInstance().getCosmoxGame().getPrefix() + "Vous laissez le choix de votre classe au §ehasard");
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(!gameStarted && event.getAction().isRightClick() && event.getItem() != null && Items.WR_KIT_ITEM.getType().equals(event.getItem().getType())) {
			event.getPlayer().openInventory(Kit.getWRInventory());
		}
	}
}
