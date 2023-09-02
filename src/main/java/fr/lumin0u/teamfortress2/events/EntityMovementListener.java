package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.ThrownExplosive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import java.util.ArrayList;

public class EntityMovementListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public EntityMovementListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onProjectileCollide(ProjectileHitEvent event) {
		for(TFPlayer player : gm.getPlayers()) {
			for(ThrownExplosive explosive : new ArrayList<>(ThrownExplosive.getLivingInstances())) {
				if(explosive.isExplodeOnProjectileCollide() && event.getEntity().equals(explosive.getEntity())) {
					event.setCancelled(true);
					
					explosive.explode();
				}
			}
		}
	}
	
	@EventHandler
	public void onEggHit(PlayerEggThrowEvent event) {
		event.setHatching(false);
	}
	
	@EventHandler
	public void onVehicleCollide(VehicleEntityCollisionEvent event) {
		event.setCancelled(true);
	}
}
