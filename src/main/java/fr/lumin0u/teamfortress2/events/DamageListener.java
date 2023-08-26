package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.MeleeWeaponType;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static fr.lumin0u.teamfortress2.FireDamageCause.WILD_FIRE;

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
		
		if(gm.getPhase().isInGame() && event.getDamager() instanceof Player) {
			TFPlayer damager = TFPlayer.of(event.getDamager());
			
			if(!damager.isSpectator() && !damager.isDead() && damager.getWeaponInHand().map(w -> w.getType() instanceof MeleeWeaponType).orElse(false)) {
				Weapon weapon = damager.getWeaponInHand().get();
				weapon.leftClick(new RayTraceResult(event.getEntity().getLocation().toVector(), event.getEntity()));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		event.setCancelled(true);
		
		TFPlayer player;
		if(gm.getPhase().isInGame() && event.getEntity() instanceof Player && !(player = TFPlayer.of(event.getEntity())).isSpectator()) {
			if(event.getCause() == DamageCause.VOID) {
				player.die(null);
			}
			if(event.getCause() == DamageCause.POISON) {
				player.damage(player.getPoisonSource(), event.getDamage(), new Vector());
			}
			if(event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.FIRE) {
				player.damage(player.getFireCause().damager(), player.getFireCause().damage(), new Vector());
			}
			if(event.getCause() == DamageCause.LAVA) {
				player.setFireCause(WILD_FIRE);
				player.damage(null, event.getDamage(), new Vector());
			}
		}
	}
	
	@EventHandler
	public void onMinecartDamage(VehicleDamageEvent event) {
		event.setCancelled(true);
	}
}
