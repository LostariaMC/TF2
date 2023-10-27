package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.managers.GameManager.GamePhase;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.types.PlaceableWeaponType;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class PlayerMovementListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public PlayerMovementListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		// pregame
		if(gm.getPhase() == GamePhase.PRE_START && event.hasChangedPosition()) {
			event.setCancelled(true);
		}
		
		if(!player.isDead()) {
			// walk in other's safezone
			if(gm.getTeams().stream().anyMatch(team -> !team.equals(player.getTeam()) && team.getSafeZone().contains(event.getTo().toVector()))) {
				event.setCancelled(true);
				player.damage(null, 1, player.toBukkit().getVelocity().multiply(-5));
				player.toBukkit().playSound(player.toBukkit().getLocation(), Sound.ENTITY_BEE_STING, 1, 2);
			}
			
			// walk in/out of own safezone
			if(player.hasLeftSafeZone() && player.getTeam().getSafeZone().contains(event.getTo().toVector())) {
				event.setCancelled(true);
			}
			if(!player.hasLeftSafeZone() && !player.isInSafeZone()) {
				player.setHasLeftSafeZone(true);
			}
			
			// walk on placeable weapons
			if(gm.getPhase().isInGame()) {
				for(TFPlayer other : tf.getPlayers()) {
					other.getWeapons().stream()
							.filter(w -> w.getType() instanceof PlaceableWeaponType)
							.flatMap(w -> new ArrayList<>(((PlaceableWeapon) w).getBlocks()).stream())
							.filter(block -> BoundingBox.of(block.getBlock()).contains(player.getLocation().toVector()))
							.forEach(block -> block.onWalkOn(player));
				}
			}
			
			// scout dash
			if(player.toBukkit().isOnGround() && player.getKit().equals(Kit.SCOUT)) {
				player.toBukkit().setAllowFlight(true);
			}
		}
	}
	
	@EventHandler
	public void onToggleFlight(PlayerToggleFlightEvent event) {
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		if(!player.isDead() && !player.toBukkit().getGameMode().equals(GameMode.CREATIVE)) {
			event.setCancelled(true);
			
			if(player.getKit().equals(Kit.SCOUT)) {
				player.toBukkit().setVelocity(new Vector(0, 0.6, 0).add(player.getEyeLocation().getDirection().multiply(player.isEnergized() ? 1.3 : 0.8)));
				player.toBukkit().setAllowFlight(false);
				
				TFSound.SCOUT_DASH.play(player.toBukkit().getLocation());
			}
		}
	}
}
