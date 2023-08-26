package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.Items;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.Scopeable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
				player.toBukkit().sendMessage(Component.text()
						.append(Component.text(TF.getInstance().getCosmoxGame().getPrefix() + "§7Vous choisissez la classe "))
						.append(Component.text(clickedKit.getName(), clickedKit.getColor(), TextDecoration.BOLD))
						.appendSpace()
						.append(Component.text(clickedKit.getSymbol(), clickedKit.getColor())));
			}
			else if(Items.randomKitItem.isSimilar(event.getCurrentItem())) {
				player.setNextKit(Kit.RANDOM, true);
				player.toBukkit().sendMessage(TF.getInstance().getCosmoxGame().getPrefix() + "§7Votre classe sera §d§lAléatoire");
				
				clickedKit = Kit.RANDOM;
			}
			
			if(clickedKit != null && player.isInSafeZone()) {
				player.respawn(player.toBukkit().getLocation());
			}
		}
	}
	
	@EventHandler
	public void onScroll(PlayerItemHeldEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		player.getWeapons(Scopeable.class).forEach(sniper -> ((Scopeable)sniper).setScoping(false));
		
		if(!player.isDead()) {
			player.getWeaponInSlot(event.getNewSlot())
					.filter(PlaceableWeapon.class::isInstance)
					.ifPresentOrElse(w -> event.getPlayer().setGameMode(GameMode.SURVIVAL), () -> event.getPlayer().setGameMode(GameMode.ADVENTURE));
		}
		
		player.getrClickingTask().resetClick();
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
