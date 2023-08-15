package fr.lumin0u.teamfortress2.events;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.GameManager.GamePhase;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.types.PlaceableWeaponType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class MoveListener implements Listener
{
	private final GameManager gm;
	private final TF tf;
	
	public MoveListener(GameManager gm, TF tf) {
		this.gm = gm;
		this.tf = tf;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		
		TFPlayer player = TFPlayer.of(event.getPlayer());
		
		if(gm.getPhase() == GamePhase.PRE_START && event.hasChangedPosition()) {
			event.setCancelled(true);
		}
		
		if(!player.isSpectator() && gm.getTeams().stream().anyMatch(team -> !team.equals(player.getTeam()) && team.getSafeZone().contains(event.getTo().toVector()))) {
			event.setCancelled(true);
			player.damage(null, 2, new Vector());
			player.toBukkit().playSound(player.toBukkit().getLocation(), Sound.ENTITY_BEE_STING, 1, 2);
		}
		
		if(gm.getPhase().isInGame() && event.hasChangedBlock()) {
			tf.getPlayers().stream().filter(player::canDamage).forEach(ennemy -> {
				ennemy.getWeapons().stream()
						.filter(w -> w.getType() instanceof PlaceableWeaponType)
						.flatMap(w -> ((PlaceableWeapon)w).getBlocks().stream())
						.filter(block -> BoundingBox.of(block.getBlock()).overlaps(player.getBodyBox()))
						.forEach(block -> block.onWalkOn(player));
			});
		}
	}
}
