package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.RocketLauncherType.Rocket;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

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
			for(Rocket rocket : WeaponTypes.ROCKET_LAUNCHER.getRockets()) {
				if(rocket.getEntity().equals(event.getEntity())) {
					event.setCancelled(true);// LOL JAI FAI KOI LA
				}
			}
		}
	}
}
