package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.MeleeWeaponType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class DamageListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public DamageListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onHitEntity(EntityDamageByEntityEvent event) {
		
		event.setCancelled(true);
		
		if(event.getDamager() instanceof Player) {
			TFPlayer damager = TFPlayer.of(event.getDamager());
			
			if(!damager.isSpectator() && !damager.isDead() && damager.getWeaponInHand().map(w -> w.getType() instanceof MeleeWeaponType).orElse(false)) {
				Weapon<? extends MeleeWeaponType> weapon = (Weapon<? extends MeleeWeaponType>) damager.getWeaponInHand().get();
				weapon.getType().leftClickAction(damager, weapon, new RayTraceResult(event.getEntity().getLocation().toVector(), event.getEntity()));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		event.setCancelled(true);
	}
}
