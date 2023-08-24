package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.Items;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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
		
		player.getWeaponInHand().ifPresent(weapon -> weapon.leftClick(null));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		event.setCancelled(true);
		
		if(event.getAction().isRightClick()) {
			player.getrClickingTask().triggerRClick();
		}
		
		if(event.getAction().isRightClick() && event.getItem() != null && Items.MENU_ITEM.getType().equals(event.getItem().getType())) {
			player.toBukkit().openInventory(Kit.getKitMenuInventory());
		}
		else if(GameManager.getInstance().getPhase().isInGame() && event.getAction().isRightClick() && !player.isInSafeZone()) {
			Vector interactPoint = event.getInteractionPoint() == null ? new Vector() : event.getInteractionPoint().toVector();
			
			RayTraceResult rayTraceResult = new RayTraceResult(interactPoint, event.getClickedBlock(), event.getBlockFace());
			
			player.getWeaponInHand().ifPresent(weapon -> weapon.rightClick(rayTraceResult));
		}
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		event.setCancelled(true);
		player.getrClickingTask().triggerRClick();
		
		if(gm.getPhase().isInGame() && !player.isInSafeZone()) {
			RayTraceResult rayTraceResult = new RayTraceResult(event.getRightClicked().getLocation().toVector(), event.getRightClicked());
			
			player.getWeaponInHand().ifPresent(weapon -> weapon.rightClick(rayTraceResult));
		}
	}
	
	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if(event.getRightClicked() instanceof ArmorStand)
			onInteractEntity(event);
	}
	
	@EventHandler
	public void onItemConsume(PlayerItemConsumeEvent event) {
		event.setCancelled(true);
	}
}
