package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class InteractListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public InteractListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onArmSwing(PlayerArmSwingEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		player.getWeaponInHand().ifPresent(weapon -> weapon.getType().leftClickAction(player, weapon, null));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		event.setCancelled(true);
		
		if(event.getAction().isRightClick() && event.getItem() != null && event.getItem().isSimilar(TF.MENU_ITEM)) {
			player.openKitMenu();
		}
		
		if(event.getAction().isRightClick()) {
			Vector interactPoint = event.getInteractionPoint() == null ? new Vector() : event.getInteractionPoint().toVector();
			
			RayTraceResult rayTraceResult = new RayTraceResult(interactPoint, event.getClickedBlock(), event.getBlockFace());
			
			player.getWeaponInHand().ifPresent(weapon -> weapon.getType().rightClickAction(player, weapon, rayTraceResult));
		}
	}
	
	/*@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		event.setCancelled(true);
	}*/
}
