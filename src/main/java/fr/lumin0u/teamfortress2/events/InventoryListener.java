package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.Items;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.Sniper;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

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
		event.setCancelled(true);
		
		TFPlayer player = TFPlayer.of(event.getWhoClicked());
		
		if(event.getInventory().equals(Kit.getKitMenuInventory())) {
			Kit clickedKit = Kit.byRepItem(event.getCurrentItem());
			
			if(clickedKit != null) {
				player.setNextKit(clickedKit, true);
				player.toBukkit().sendMessage(TF.getInstance().getCosmoxGame().getPrefix() + "Vous choisissez la classe §e" + clickedKit.getName());
				
			}
			else if(Items.randomKitItem.isSimilar(event.getCurrentItem())) {
				player.setNextKit(Kit.RANDOM, true);
				player.toBukkit().sendMessage(TF.getInstance().getCosmoxGame().getPrefix() + "Votre classe sera choisie aléatoirement");
				
				clickedKit = Kit.RANDOM;
			}
			
			if(clickedKit != null && player.isInSafeZone()) {
				player.respawn(player.toBukkit().getLocation());
			}
		}
	}
	
	@EventHandler
	public void onScroll(PlayerItemHeldEvent event) {
		TFPlayer.of(event.getPlayer()).<WeaponType, Sniper>getOptWeapon(WeaponTypes.SNIPER).ifPresent(sniper -> sniper.setScoping(false));
		
		TFPlayer.of(event.getPlayer()).getWeaponInSlot(event.getNewSlot())
				.filter(PlaceableWeapon.class::isInstance)
				.ifPresentOrElse(w -> event.getPlayer().setGameMode(GameMode.SURVIVAL), () -> event.getPlayer().setGameMode(GameMode.ADVENTURE));
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onSwapHand(PlayerSwapHandItemsEvent event) {
		event.setCancelled(true);
	}
}
