package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.GameManager.GamePhase;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class InventoryListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public InventoryListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getInventory().equals(Kit.getKitMenuInventory())) {
			event.setCancelled(true);
			
			onClickInChoseKitInventory(event);
		}
	}
	
	/**
	 * NO @EventHandler !!!
	 * */
	public static void onClickInChoseKitInventory(InventoryClickEvent event) {
		Kit clickedKit = Kit.byRepItem(event.getCurrentItem());
		if(clickedKit != null) {
			TF.getInstance().setKitInRedis(WrappedPlayer.of(event.getWhoClicked()), clickedKit);
		}
		else if(Kit.randomKitItem.isSimilar(event.getCurrentItem())) {
			TF.getInstance().setKitInRedis(WrappedPlayer.of(event.getWhoClicked()), Kit.RANDOM);
		}
	}
}
